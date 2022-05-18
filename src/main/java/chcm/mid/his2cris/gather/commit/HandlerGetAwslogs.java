package chcm.mid.his2cris.gather.commit;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.CloudWatchLogsEvent;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HandlerGetAwslogs implements RequestHandler<CloudWatchLogsEvent, String> {

    @Override
    public String handleRequest(CloudWatchLogsEvent event, Context context) {
        String awsLogs = event.toString();
    	context.getLogger().log("Input: " + awsLogs);        
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

        return "";
    }

}
