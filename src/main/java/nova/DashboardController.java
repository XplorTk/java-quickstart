package nova;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import nova.helpers.Utils;

@Controller
public class DashboardController {

	/**
	 * Here is a sample internal dashboard, where your loan officer might view applicant profiles
	 */
	@GetMapping("/dashboard")
	public String dashboard(Model model) {
		model.addAttribute("hasReceivedReportData", Utils.receivedReportData != null);

		// Pass the Nova Credit Passport data, if we've received it, to the dashboard view
		if (Utils.receivedReportData != null) {
			model.addAttribute("userArgs", Utils.receivedReportData.get("userArgs"));
			model.addAttribute("publicToken", Utils.receivedReportData.get("publicToken"));
			model.addAttribute("applicantName", Utils.receivedReportData.get("applicantName"));
			model.addAttribute("applicantEmail", Utils.receivedReportData.get("applicantEmail"));
			model.addAttribute("novaScore", Utils.receivedReportData.get("novaScore"));
			model.addAttribute("borrowLoanDecision", Utils.receivedReportData.get("borrowLoanDecision"));
		}

		return "dashboard";
	}
 
}
