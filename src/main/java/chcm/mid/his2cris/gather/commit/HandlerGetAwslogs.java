package chcm.mid.his2cris.gather.commit;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.CloudWatchLogsEvent;

public class HandlerGetAwslogs implements RequestHandler<CloudWatchLogsEvent, String> {

    @Override
    public String handleRequest(CloudWatchLogsEvent event, Context context) {
        context.getLogger().log("Input: " + event.toString());
        return "";
    }

}
