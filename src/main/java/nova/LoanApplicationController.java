package nova;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoanApplicationController {

	/**
	 * IMPORTANT! Your credentials should NOT be left unencrypted in your production integration
	 * We recommend placing them in a hidden environment variable / file.
	 * The variable file here is left unencrypted for demonstration purposes only
	 */

	@Value("${NOVA_PUBLIC_ID}")
	private String novaPublicId;
	
	@Value("${NOVA_ENV}")
	private String novaEnv;
	
	@Value("${NOVA_PRODUCT_ID}")
	private String novaProductId;
	
	private String novaUserArgs = "borrow_loan_id_12345";
	
	/**
	 * Here is a sample loan application that has the NovaConnect widget added.
	 * NovaConnect is a preconfigured modal pop up that gets attached with a single line of Javascript
	 * More details: https://www.novacredit.com/quickstart-guide#clientside
	 */
	@GetMapping("/")
	public String index(Model model) {
		/**
		 * Pass our Nova configs to the template so the widget can render
		 * We can also pass a string of data to `userArgs` of NovaConnect, and this string will be returned in our webhook
		 * Example userArgs: unique identifiers from your system, unique nonces for security
		 */
		model.addAttribute("novaPublicId", novaPublicId);
		model.addAttribute("novaEnv", novaEnv);
		model.addAttribute("novaProductId", novaProductId);
		model.addAttribute("novaUserArgs", novaUserArgs);

		return "loan_application";
	}

}
