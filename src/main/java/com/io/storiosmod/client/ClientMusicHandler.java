package com.io.storiosmod.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Set;

public class ClientMusicHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long MAX_DOWNLOAD_BYTES = 50L * 1024L * 1024L;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "audio/ogg", "audio/vorbis", "audio/wav", "audio/wave",
            "audio/x-wav", "audio/mpeg", "audio/mp3");

    private static volatile int alSource = -1;
    private static volatile int alBuffer = -1;
    private static volatile Thread downloadThread;

    private static volatile String currentCategory = "master";
    private static volatile float currentVolume = 1.0f;

    public static void register() {
        NeoForge.EVENT_BUS.register(ClientMusicHandler.class);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (alSource != -1) {
            try {
                int state = AL10.alGetSourcei(alSource, AL10.AL_SOURCE_STATE);
                if (state == AL10.AL_PLAYING) {
                    float gain = computeGain(currentCategory, currentVolume);
                    AL10.alSourcef(alSource, AL10.AL_GAIN, gain);
                }
            } catch (Exception ignored) {
            }
        }
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        stop();
    }

    public static void play(String url, String category, float volume) {
        currentCategory = category;
        currentVolume = volume;
        cleanupAL();
        Thread oldThread = downloadThread;
        if (oldThread != null) {
            oldThread.interrupt();
            downloadThread = null;
        }

        Thread thread = new Thread(() -> {
            Thread self = Thread.currentThread();
            try {
                sendActionBar("\u00A7e[Music] \u00A7fConnecting...");

                byte[] audioData = downloadWithProgress(url);
                if (audioData == null || downloadThread != self)
                    return;

                sendActionBar("\u00A7e[Music] \u00A7fDecoding audio...");

                String extension = extractExtension(url);
                short[] pcmSamples;
                int channels;
                int sampleRate;

                if (".ogg".equals(extension)) {
                    int[] info = new int[2];
                    pcmSamples = decodeOgg(audioData, info);
                    if (pcmSamples == null) {
                        sendError("Failed to decode OGG audio.");
                        return;
                    }
                    channels = info[0];
                    sampleRate = info[1];
                } else if (".wav".equals(extension)) {
                    int[] info = new int[2];
                    pcmSamples = decodeWav(audioData, info);
                    if (pcmSamples == null) {
                        sendError("Failed to decode WAV audio.");
                        return;
                    }
                    channels = info[0];
                    sampleRate = info[1];
                } else {
                    sendError("Unsupported audio format: " + extension);
                    return;
                }

                if (pcmSamples.length == 0) {
                    sendError("Audio file is empty.");
                    return;
                }

                if (downloadThread != self)
                    return;

                final short[] finalPcm = pcmSamples;
                final int finalChannels = channels;
                final int finalSampleRate = sampleRate;

                Minecraft.getInstance().execute(() -> {
                    if (downloadThread != self)
                        return;
                    float gain = computeGain(category, volume);
                    playWithOpenAL(finalPcm, finalChannels, finalSampleRate, gain);
                    sendActionBar("\u00A7a[Music] \u00A7fNow playing (" + category + ")");
                });
            } catch (InterruptedException ignored) {
            } catch (Exception e) {
                LOGGER.error("Failed to play music", e);
                sendError(e.getMessage() != null ? e.getMessage() : "Unknown playback error");
            }
        });
        thread.setDaemon(true);
        downloadThread = thread;
        thread.start();
    }

    public static void stop() {
        boolean wasPlaying = false;
        Thread thread = downloadThread;
        if (thread != null) {
            thread.interrupt();
            downloadThread = null;
            wasPlaying = true;
        }
        if (alSource != -1 || alBuffer != -1) {
            cleanupAL();
            wasPlaying = true;
        }

        if (wasPlaying) {
            sendActionBar("\u00A7c[Music] \u00A7fStopped");
        }
    }

    private static float computeGain(String category, float volume) {
        Minecraft mc = Minecraft.getInstance();
        SoundSource source = parseSoundSource(category);
        float categoryVolume = mc.options.getSoundSourceVolume(source);
        float masterVolume = mc.options.getSoundSourceVolume(SoundSource.MASTER);
        if (source == SoundSource.MASTER) {
            return volume * masterVolume;
        }
        return volume * categoryVolume * masterVolume;
    }

    private static SoundSource parseSoundSource(String name) {
        switch (name.toLowerCase()) {
            case "master":
                return SoundSource.MASTER;
            case "music":
                return SoundSource.MUSIC;
            case "record":
                return SoundSource.RECORDS;
            case "weather":
                return SoundSource.WEATHER;
            case "block":
                return SoundSource.BLOCKS;
            case "hostile":
                return SoundSource.HOSTILE;
            case "neutral":
                return SoundSource.NEUTRAL;
            case "player":
                return SoundSource.PLAYERS;
            case "ambient":
                return SoundSource.AMBIENT;
            case "voice":
                return SoundSource.VOICE;
            default:
                return SoundSource.MASTER;
        }
    }

    private static void cleanupAL() {
        if (alSource != -1) {
            AL10.alSourceStop(alSource);
            AL10.alDeleteSources(alSource);
            alSource = -1;
        }
        if (alBuffer != -1) {
            AL10.alDeleteBuffers(alBuffer);
            alBuffer = -1;
        }
    }

    private static byte[] downloadWithProgress(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);
        connection.setInstanceFollowRedirects(true);

        try {
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                sendError("HTTP error " + responseCode);
                return null;
            }

            String contentType = connection.getContentType();
            if (contentType != null) {
                contentType = contentType.split(";")[0].trim().toLowerCase();
                if (!contentType.startsWith("audio/") && !ALLOWED_CONTENT_TYPES.contains(contentType)) {
                    sendError("Not an audio file (type: " + contentType + ")");
                    return null;
                }
            }

            long contentLength = connection.getContentLengthLong();

            try (InputStream in = connection.getInputStream();
                    ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                long totalRead = 0;
                int bytesRead;
                int lastPercent = -1;

                while ((bytesRead = in.read(buffer)) != -1) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }
                    totalRead += bytesRead;
                    if (totalRead > MAX_DOWNLOAD_BYTES) {
                        sendError("File too large (max 50 MB)");
                        return null;
                    }
                    out.write(buffer, 0, bytesRead);

                    if (contentLength > 0) {
                        int percent = (int) ((totalRead * 100) / contentLength);
                        if (percent != lastPercent) {
                            lastPercent = percent;
                            String bar = buildProgressBar(percent);
                            sendActionBar("\u00A7e[Music] \u00A7f" + bar + " \u00A7a" + percent + "%");
                        }
                    } else {
                        long kb = totalRead / 1024;
                        sendActionBar("\u00A7e[Music] \u00A7fDownloading: \u00A7a" + kb + " KB");
                    }
                }

                sendActionBar("\u00A7e[Music] \u00A7fDownload complete \u00A7a100%");
                return out.toByteArray();
            }
        } finally {
            connection.disconnect();
        }
    }

    private static String buildProgressBar(int percent) {
        int filled = percent / 5;
        int empty = 20 - filled;
        StringBuilder sb = new StringBuilder("\u00A78[\u00A7a");
        for (int i = 0; i < filled; i++)
            sb.append("|");
        sb.append("\u00A77");
        for (int i = 0; i < empty; i++)
            sb.append("|");
        sb.append("\u00A78]");
        return sb.toString();
    }

    private static void playWithOpenAL(short[] pcmData, int channels, int sampleRate, float gain) {
        cleanupAL();

        int buf = AL10.alGenBuffers();
        int format = channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;
        ShortBuffer shortBuffer = BufferUtils.createShortBuffer(pcmData.length);
        shortBuffer.put(pcmData).flip();
        AL10.alBufferData(buf, format, shortBuffer, sampleRate);

        int error = AL10.alGetError();
        if (error != AL10.AL_NO_ERROR) {
            LOGGER.error("OpenAL buffer error: {}", error);
            sendError("Audio buffer error (code " + error + ")");
            AL10.alDeleteBuffers(buf);
            return;
        }

        int src = AL10.alGenSources();
        AL10.alSourcei(src, AL10.AL_BUFFER, buf);
        AL10.alSourcef(src, AL10.AL_GAIN, gain);
        AL10.alSourcePlay(src);

        error = AL10.alGetError();
        if (error != AL10.AL_NO_ERROR) {
            LOGGER.error("OpenAL source error: {}", error);
            sendError("Audio playback error (code " + error + ")");
            AL10.alDeleteSources(src);
            AL10.alDeleteBuffers(buf);
            return;
        }

        alBuffer = buf;
        alSource = src;
    }

    private static short[] decodeOgg(byte[] data, int[] outInfo) {
        ByteBuffer audioBuffer = MemoryUtil.memAlloc(data.length);
        try {
            audioBuffer.put(data).flip();
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer error = stack.mallocInt(1);
                long decoder = STBVorbis.stb_vorbis_open_memory(audioBuffer, error, null);
                if (decoder == 0) {
                    LOGGER.error("STBVorbis open failed with error code: {}", error.get(0));
                    return null;
                }

                STBVorbisInfo info = STBVorbisInfo.malloc(stack);
                STBVorbis.stb_vorbis_get_info(decoder, info);

                int channels = info.channels();
                int sampleRate = info.sample_rate();
                int totalSamples = STBVorbis.stb_vorbis_stream_length_in_samples(decoder);

                LOGGER.info("OGG info: channels={}, sampleRate={}, totalSamples={}", channels, sampleRate,
                        totalSamples);

                outInfo[0] = channels;
                outInfo[1] = sampleRate;

                if (totalSamples == 0) {
                    STBVorbis.stb_vorbis_close(decoder);
                    return new short[0];
                }

                ShortBuffer pcmBuffer = MemoryUtil.memAllocShort(totalSamples * channels);
                try {
                    int decoded = STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, pcmBuffer);
                    int totalShorts = decoded * channels;
                    short[] samples = new short[totalShorts];
                    pcmBuffer.position(0);
                    pcmBuffer.limit(totalShorts);
                    pcmBuffer.get(samples);
                    return samples;
                } finally {
                    MemoryUtil.memFree(pcmBuffer);
                    STBVorbis.stb_vorbis_close(decoder);
                }
            }
        } finally {
            MemoryUtil.memFree(audioBuffer);
        }
    }

    private static short[] decodeWav(byte[] data, int[] outInfo) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new ByteArrayInputStream(data));
            AudioFormat fmt = ais.getFormat();
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    fmt.getSampleRate(), 16, fmt.getChannels(),
                    fmt.getChannels() * 2, fmt.getSampleRate(), false);
            AudioInputStream converted;
            if (fmt.getEncoding() == AudioFormat.Encoding.PCM_SIGNED && fmt.getSampleSizeInBits() == 16) {
                converted = ais;
            } else {
                converted = AudioSystem.getAudioInputStream(targetFormat, ais);
            }
            byte[] pcmBytes = converted.readAllBytes();
            ShortBuffer sb = ByteBuffer.wrap(pcmBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
            short[] samples = new short[sb.remaining()];
            sb.get(samples);
            outInfo[0] = fmt.getChannels();
            outInfo[1] = (int) fmt.getSampleRate();
            return samples;
        } catch (Exception e) {
            LOGGER.error("WAV decode failed", e);
            return null;
        }
    }

    private static void sendActionBar(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.execute(() -> {
                if (mc.gui != null) {
                    mc.gui.setOverlayMessage(Component.literal(message), false);
                }
            });
        }
    }

    private static void sendError(String message) {
        LOGGER.error("[Music] {}", message);
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.execute(() -> {
                if (mc.player != null) {
                    mc.player.displayClientMessage(
                            Component.literal("\u00A7c[Music] Error: \u00A7f" + message), false);
                }
            });
        }
    }

    private static String extractExtension(String url) {
        String path = url;
        int queryIndex = path.indexOf('?');
        if (queryIndex != -1)
            path = path.substring(0, queryIndex);
        int hashIndex = path.indexOf('#');
        if (hashIndex != -1)
            path = path.substring(0, hashIndex);
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex != -1)
            return path.substring(dotIndex).toLowerCase();
        return "";
    }
}

