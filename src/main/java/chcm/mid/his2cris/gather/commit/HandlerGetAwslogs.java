package chcm.mid.his2cris.gather.commit;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.CloudWatchLogsEvent;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONObject;

public class HandlerGetAwslogs implements RequestHandler<CloudWatchLogsEvent, String> {

	private static final String USELESS_COMMIT = "802560 Query";
	private boolean need2EC2 = true;
	
    @Override
    public String handleRequest(CloudWatchLogsEvent event, Context context) {
    	LambdaLogger logger = context.getLogger();
    	context.getLogger().log(event.toString());
    	String awsLogs = event.getAwsLogs().getData();
        Decoder decoder = Base64.getDecoder();
		byte[] decodedEvent = decoder.decode(awsLogs);
        StringBuilder output = new StringBuilder();
        try {
            GZIPInputStream inputStream = new GZIPInputStream(new ByteArrayInputStream(decodedEvent));
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            bufferedReader.lines().forEach( line -> {
              logger.log(line);
              output.append(line);
            });
            
            JSONObject obj = new JSONObject(output.toString());
            JSONArray arr = obj.getJSONArray("logEvents");
            for (int i = 0; i < arr.length(); ++i) {
	        	  JSONObject arrObj = arr.getJSONObject(i);
	        	  String message = arrObj.getString("message");
	        	  if(message.contains(USELESS_COMMIT)) {
	        		  need2EC2 = false;
	        		  break;
	        	  }
            }
        } catch(IOException e) {
        	context.getLogger().log("ERROR: " + e.toString());
            logger.log("ERROR: " + e.toString());
        }   	
    	if(need2EC2) {
    		passAWSlog2EC2(context, awsLogs);
    	}else
    		context.getLogger().log("排除....." + USELESS_COMMIT);
    	
        return "";
    }

    private void passAWSlog2EC2(Context context, String awsLogs) {
    	StringBuffer sb = new StringBuffer("");
		context.getLogger().log("開始連線至AP_EC2......");
		try {
			URL url = new URL("http://10.62.3.6:8080/his2cris/GetAwslogsServlet");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();			
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("Content-Type", "text/plain");
			connection.connect();
			context.getLogger().log("連線至AP_EC2成功......");
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			out.write(awsLogs.getBytes());
			out.flush();
			out.close();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String lines;
			while ((lines = reader.readLine()) != null) {
				lines = new String(lines.getBytes(), "utf-8");
				sb.append(lines);
			}
			reader.close();
			//context.getLogger().log("顯示呼叫結果:" + sb.toString());
			// 斷開連線
			connection.disconnect();
			context.getLogger().log("AP_EC2連線已斷開......");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			context.getLogger().log("Caught MalformedURLException exception: " + e.getMessage());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			context.getLogger().log("Caught UnsupportedEncodingException exception: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			context.getLogger().log("Caught IOException exception: " + e.getMessage());
		}
    }
}
