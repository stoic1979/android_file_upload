package com.example.imageuploadtest;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.util.Log;

public class HttpUtils {
	
	private static final String TAG = "HttpUtils";

	/**
	 * 
	 * @param upLoadServerUri    server url to upload file
	 * @param filePath           absolute path of file on device
	 * @param fileId             id/name to be sent to server as server uses this
	 *                           as a filename or identity
	 * @return                   response code from server, 200 for HTTP OK
	 */
	public static int uploadFile(String upLoadServerUri, 
			String filePath, 
			String fileId,
			FileUploadObserver uploadObserver) {

		HttpURLConnection 			conn = null;
		DataOutputStream 			dos = null;
		String 						lineEnd = "\r\n";
		String 						twoHyphens = "--";
		String 						boundary = "*****";
		int							bytesRead, bytesAvailable, bufferSize;
		byte[] 						buffer;
		int 						maxBufferSize = 1 * 1024 * 1024;
		File						sourceFile = new File(filePath);
		int 						serverResponseCode = 0;	
		long						totalBytesRead = 0;

		try {
			// open a URL connection to the server
			FileInputStream fileInputStream = new FileInputStream(sourceFile);
			URL url = new URL(upLoadServerUri);
			
			//
			// Open a HTTP connection to the URL
			//
			conn = (HttpURLConnection) url.openConnection();
			conn. setDoInput(true); // Allow Inputs
			conn. setDoOutput(true); // Allow Outputs
			conn. setUseCaches(false); // Don't use a Cached Copy
			conn. setRequestMethod("POST");
			conn. setRequestProperty("Connection", "Keep-Alive");
			conn. setRequestProperty("ENCTYPE", "multipart/form-data");
			conn. setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

			dos = new DataOutputStream(conn.getOutputStream());

			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"" + fileId  +   "\";"
					+ " filename=\"" + filePath + "\"" + lineEnd);
			dos.writeBytes(lineEnd);

			// create a buffer of maximum size
			bytesAvailable = fileInputStream.available();

			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			// read file and write it into form...
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			totalBytesRead += bytesRead;

			while (bytesRead > 0) {

				
				sendFileUploadProgress(uploadObserver, totalBytesRead, sourceFile.length());
				
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				totalBytesRead += bytesRead;

			}

			// send multipart form data necesssary after file data...
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// Responses from the server (code and message)
			serverResponseCode = conn.getResponseCode();
			final String serverResponseMessage = conn.getResponseMessage();

			Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage
					+ ": " + serverResponseCode);

			Log.d(TAG, "=====================================================================");
			Log.d(TAG, "====> file upload: server response code: " + serverResponseCode);
			Log.d(TAG, "====> file upload: server message:-\n" + serverResponseMessage);
			Log.d(TAG, "=====================================================================");
			
			// close the streams //
			fileInputStream.close();
			dos.flush();
			dos.close();

		} catch (MalformedURLException ex) {

			Log.d(TAG, "------------ MalformedURLException EXCEPTION -----------------------");
			ex.printStackTrace();

			
		} catch (Exception e) {

			Log.d(TAG, "------------ File UPLOAD EXCEPTION -----------------------");
			e.printStackTrace();
		}

		return serverResponseCode;

	} // End else block
	
	private static void sendFileUploadProgress(FileUploadObserver uploadObserver, long totalBytesRead, long totalBytes) {
		if(uploadObserver != null) {
			uploadObserver.uploadStatusUpdate(0, totalBytesRead, totalBytes);
		}
	}

}// HttpUtils
