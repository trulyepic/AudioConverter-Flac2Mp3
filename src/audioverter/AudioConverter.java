package audioverter;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Arrays;

import it.sauronsoftware.jave.*;

import io.nayuki.flac.common.StreamInfo;
import io.nayuki.flac.decode.FlacDecoder;

/**
 * Class to perform audio conversion from .flac file to .mp3 file.
 * 
 * @author Kenneth --> just the implementation on this project 
 * sauronsoftware manual link: https://www.sauronsoftware.it/projects/jave/manual.php - wav to mp3
 * Nayuki github Repo Flac2Wav --> https://github.com/nayuki/FLAC-library-Java/blob/master/src/io/nayuki/flac/app/DecodeFlacToWav.java?ts=4
 *
 */
public class AudioConverter {
	
	
	/**
	 * Path of Source directory
	 */
	//private static String SOURCE_DIR = System.getProperty("user.dir") + "/src/main/audio_files/flac/";
	private static String SOURCE_DIR = "C:\\Users\\NwoyeK\\Desktop\\audio\\";
	
	private static DataOutputStream out;

	/**
	 * @param assetId name of file to be converted
	 * @param startAt start time from where the conversion starts
	 * @return String message to represent the success or error.
	 * @throws EncoderException If there is any exception while encoding the audio
	 * @throws IOException Raised while accessing audio file.
	 */
	public String Flac2Mp3(String assetId, float startAt) throws EncoderException, IOException {
		
		//String TARGET_DIR = System.getProperty("user.dir") + "/src/main/audio_files/mp3/";
		String TARGET_DIR = "C:\\Users\\NwoyeK\\Desktop\\audio\\";
		AudioConverter ac = new AudioConverter();
		
		float conversionDuration = 30.0f;
		float offsetDuration = startAt;
		int BitRate = 128000;
		int Channels = 2;
		int samplingRate = 44100;
		
		String HELPER = AudioConverter.SOURCE_DIR + "helper.wav";
        String source_file, source_file_path = "";
        String target_file, target_file_path = "";
        
        source_file = assetId;
        
        if (source_file.indexOf(".") > 0) 
        {
        	target_file = source_file.substring(0, source_file.indexOf("."));
        }
        else
        {
        	target_file = source_file;
        }
        	
        
        
        String valid_source_file = AudioConverter.checkFilename(source_file, ".flac");
        String valid_target_file = AudioConverter.checkFilename(target_file, ".mp3");
        
        source_file = AudioConverter.extensionFix(valid_source_file, source_file, ".flac");
        target_file = AudioConverter.extensionFix(valid_target_file, target_file, ".mp3");
        
        if ((valid_source_file.equalsIgnoreCase("1") || valid_source_file.equalsIgnoreCase("2")) && (valid_target_file.equalsIgnoreCase("1") || valid_target_file.equalsIgnoreCase("2")))
        {
        	source_file_path += AudioConverter.SOURCE_DIR + source_file;
            target_file_path += TARGET_DIR  + target_file;
        	
            File source = new File(source_file_path);
            File helper_file = new File(HELPER);
            
            try
            {
            	String wav_created = ac.Flac2Wav(source_file, "helper.wav");
            	if (!wav_created.equalsIgnoreCase("Wav File Generated Successfully"))
            	{
            		return "Error! While Converting FLAC to WAV Format.";
            	}
            	
            }
            catch(Exception ex)
            {
            	return ex.getMessage();
            }
     		
            
            if (source.exists())
            {
                File target = new File(target_file_path);
                
                AudioAttributes audio = new AudioAttributes();
                audio.setCodec("libmp3lame");
                audio.setBitRate(new Integer(BitRate));
                audio.setChannels(new Integer(Channels));
                audio.setSamplingRate(new Integer(samplingRate));
                
                EncodingAttributes attrs = new EncodingAttributes();
                attrs.setFormat("mp3");
                attrs.setOffset(new Float(offsetDuration));
                attrs.setDuration(new Float(conversionDuration));
                attrs.setAudioAttributes(audio);
                
                Encoder encoder = new Encoder();
                try
                {
                	encoder.encode(helper_file, target, attrs);
                	helper_file.delete();
                	return "1";
                	
                }
                catch (Exception ex)
                {
                	helper_file.delete();
                	return ex.getMessage();
                }
                
            }
            else
            {
                return "Error: File doesn't exist path.";
            }
        }
        else
        {
            return "Error! Invalid Filename!";
        }
		
	}
	
	/**
	 * Method to convert audio file from .flac to .wav format.
	 * 
	 * @param source_file Name of source file (.flac). File to be converted.
	 * @param target_file Name of target file (.wav). Name of file that is produced after conversion.
	 * @return String message represting the success or failure.
	 * @throws IOException raised while opening or modifying audio files.
	 */
	public String Flac2Wav(String source_file, String target_file) throws IOException {
		
		String valid_source_file = AudioConverter.checkFilename(source_file, ".flac");
		String valid_target_file;
		if (target_file.equals("")){
			valid_target_file = AudioConverter.checkFilename(source_file, ".wav");
		}
		else
		{
			valid_target_file = AudioConverter.checkFilename(target_file, ".wav");	
		}
        
        source_file = AudioConverter.extensionFix(valid_source_file, source_file, ".flac");
        target_file = AudioConverter.extensionFix(valid_target_file, target_file, ".wav");
        
        if ((valid_source_file.equalsIgnoreCase("1") || valid_source_file.equalsIgnoreCase("2")) && (valid_target_file.equalsIgnoreCase("1") || valid_target_file.equalsIgnoreCase("2")))
        {
        	String source_file_path = AudioConverter.SOURCE_DIR + source_file;
    		String target_file_path = AudioConverter.SOURCE_DIR + target_file;
    		
    		File source = new File(source_file_path);
            File target = new File(target_file_path);
            
            StreamInfo streamInfo;
     		int[][] samples;
     		
     		try (FlacDecoder dec = new FlacDecoder(source)) 
     		{
     			while (dec.readAndHandleMetadataBlock() != null);
     			streamInfo = dec.streamInfo;
     			
     			if (streamInfo.sampleDepth % 8 != 0)
     			{
     				return "Only whole-byte sample depth supported";
     			}
     			
     			samples = new int[streamInfo.numChannels][(int)streamInfo.numSamples];
     			for (int off = 0; ;) 
     			{
     				int len = dec.readAudioBlock(samples, off);
     				if (len == 0) 
     				{
     					break;
     				}
     				off += len;
     			}
     		}
     		
     		byte[] expectHash = streamInfo.md5Hash;
     		if (Arrays.equals(expectHash, new byte[16])) 
     		{
     			return "Warning: MD5 hash field was blank";
     		}
     		else if (!Arrays.equals(StreamInfo.getMd5Hash(samples, streamInfo.sampleDepth), expectHash)) 
     		{
     			return "MD5 hash check failed";
     		}
     		
     		int bytesPerSample = streamInfo.sampleDepth / 8;
     		
     		try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(target)))) 
     		{
     			AudioConverter.out = out;
     			
     			int sampleDataLen = samples[0].length * streamInfo.numChannels * bytesPerSample;
     			out.writeInt(0x52494646);
     			writeLittleInt32(sampleDataLen + 36);
     			out.writeInt(0x57415645);
     			
     			out.writeInt(0x666D7420);
     			writeLittleInt32(16);
     			writeLittleInt16(0x0001);
     			writeLittleInt16(streamInfo.numChannels);
     			writeLittleInt32(streamInfo.sampleRate);
     			writeLittleInt32(streamInfo.sampleRate * streamInfo.numChannels * bytesPerSample);
     			writeLittleInt16(streamInfo.numChannels * bytesPerSample);
     			writeLittleInt16(streamInfo.sampleDepth);
     			
     			out.writeInt(0x64617461);
     			writeLittleInt32(sampleDataLen);
     			for (int i = 0; i < samples[0].length; i++) 
     			{
     				for (int j = 0; j < samples.length; j++) 
     				{
     					int val = samples[j][i];
     					if (bytesPerSample == 1) 
     					{
     						out.write(val + 128);
     					}
     					else 
     					{
     						for (int k = 0; k < bytesPerSample; k++) 
     						{
     							out.write(val >>> (k * 8));
     						}
     					}
     				}
     			}
     		}
     		
    		return "Wav File Generated Successfully";
            
        }
		
		return "Filename Error";
	}
	
	
	private static String checkFilename(String filename, String extension)
    {
        String valid;
        int name_length, extension_exists;
        
        name_length = filename.length();
        extension_exists = filename.indexOf('.');
        
        if (extension_exists > 0)
        {
            int count_dots = 0;
            int temp = extension_exists;
            while (temp > 0)
            {
                count_dots += 1;
                temp = filename.indexOf('.', extension_exists + 1);                
            }
            if (count_dots > 1)
            {
            	valid = "Error: Invalid Filename, Filename contain multiple dot (.) characters!";
            }
            else
            {
                if ((name_length - extension_exists) == extension.length())
                {
                    String ext = filename.substring(extension_exists);
                    if (! ext.equalsIgnoreCase(extension))
                    {
                    	valid = "Error: Invalid File Extension!";
                    }
                    else
                    {
                        valid = "1";
                    }
                }
                else
                {
                    valid = "Error: Invalid File Extension!";
                }
            }
        }
        else if (extension_exists == 0) 
        {
        	valid = "Error: Only Extension of the File is entered!";
        }
        else
        {
            filename += extension;
            valid = "2";
        }
        
        return valid;
    }
    
	private static String extensionFix(String code, String filename, String ext) {
		if (code.equalsIgnoreCase("2"))
        {
            filename += ext;
        }
		return filename;
	}
	
	private static void writeLittleInt16(int x) throws IOException 
	{
		out.writeShort(Integer.reverseBytes(x) >>> 16);
	}
	
	private static void writeLittleInt32(int x) throws IOException 
	{
		out.writeInt(Integer.reverseBytes(x));
	}
}
