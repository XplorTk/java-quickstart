package nova;

import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import nova.helpers.Utils;

@Controller
public class NovaController {
	
	@Autowired
	private Utils utils = new Utils();
	
	/**
	 * Route to handle Nova callback webhook, which you should specify on the dashboard as "https://your_domain_here.com/nova"
	 * This route is POST'd to after an applicant completes NovaConnect, and we have updated the status of their NovaCredit Passport
	 * When running this locally, you'll need a tunnel service like ngrok to expose your localhost: https://ngrok.com/
	 * See our docs for a list of potential responses: https://docs.neednova.com/#error-codes-amp-responses
	 */
	@PostMapping("/nova")
	ResponseEntity<?> add(@RequestBody Map<String,String> body) {
		String status = body.get("status");
		String publicToken = body.get("publicToken");
		String userArgs = body.get("userArgs");
		
		if (status.equals("SUCCESS")) {
			utils.handleNovaWebhook(publicToken, userArgs);
		} else {
			/*
			 * Handle unsuccessful statuses here, such as applicant NOT_FOUND and NOT_AUTHENTICATED
			 * For example, you might finalize your loan decision
			 */
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).warning(String.format("Report status %s received for Nova public token %s", status, publicToken));
		}
		
		return ResponseEntity.ok().build();
	}
}
