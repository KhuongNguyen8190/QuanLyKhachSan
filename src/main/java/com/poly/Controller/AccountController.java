package com.poly.Controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.poly.Bean.Account;
import com.poly.Bean.AccountMap;
import com.poly.Bean.MailInformation;
import com.poly.DAO.AccountDAO;
import com.poly.Service.LoginResponse;
import com.poly.Service.MailServiceImplement;
import com.poly.Service.PasswordUtil;
import com.poly.Service.UserDetailsServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class AccountController {
	@Autowired
	AccountDAO dao;
	@Autowired
	HttpServletRequest request;
	@Autowired
	UserDetailsServiceImpl service;
	@Autowired
	HttpSession session;
	@Autowired
	MailServiceImplement mailServiceImplement;
	@Autowired
	PasswordUtil passwordUtil;
	@Autowired
	BCryptPasswordEncoder pe;

	@GetMapping("/sign-up")
	public String signUp() {
		return "user/sign-up";
	}

	@GetMapping("/sign-in")
	public String login() {
		return "user/sign-in";
	}

	@GetMapping("/forgot-password")
	public String forgotPassword() {
		return "user/forgot-password";
	}

	@GetMapping("/forgot-password-finally")
	public String forgotPasswordFinal() {
		return "user/forgot-password-finally";
	}

	@GetMapping("/change-password")
	public String changePassword() {
		return "user/change-password";
	}

	@PostMapping("/change-password")
	@ResponseBody
	public ResponseEntity<LoginResponse> changePassword(@RequestParam("password") String password,
			@RequestParam("newpassword") String newpassword, @RequestParam("repassword") String repassword) {
		LoginResponse response = new LoginResponse();
		String message = "";
		response.setSuccess(false);
		String key = dao.findKeyByUsername((String) session.getAttribute("username"));
		Account account = dao.findByKey(key);
		if (pe.matches(password, account.getPassword())) {
			if (newpassword.equals(repassword)) {
				account.setPassword(newpassword);
				dao.update(key, account);
				message = "Đổi mật khẩu thành công!";
				response.setSuccess(true);
			} else {
				message = "Đổi mật khẩu thất bại!";
			}
		} else {
			message = "Mật khẩu hiện tại không chính xác!";
		}
		response.setMessage(message);
		return ResponseEntity.ok(response);
	}

	@RequestMapping("/auth/login/success")
	@ResponseBody
	public ResponseEntity<LoginResponse> success(Model model) {
		LoginResponse response = new LoginResponse();
		response.setSuccess(true);
		response.setMessage("Đăng nhập thành công!");
		return ResponseEntity.ok(response);
	}

	@GetMapping("/auth/login/error")
	@ResponseBody
	public ResponseEntity<LoginResponse> loginError() {
		LoginResponse response = new LoginResponse();
		String message = "";
		response.setSuccess(false);
		Account account = dao.findByUsername((String) session.getAttribute("username"));
		String passLogin = (String) session.getAttribute("password");
		if (account == null) {
			message = "Tài khoản không tồn tại!";
		} else {
			if (!pe.matches(passLogin, account.getPassword())) {
				message = "Mật khẩu không chính xác!";
			} else {
				response.setSuccess(true);
			}
		}
		response.setMessage(message);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/sign-up")
	@ResponseBody
	public ResponseEntity<LoginResponse> register(Model model, @RequestParam("username") String username,
			@RequestParam("password") String password, @RequestParam("repassword") String repassword) {
		Account ac = new Account();
		LoginResponse response = new LoginResponse();
		String message = "";
		response.setSuccess(false);
		if (!username.equals("") && !password.equals("") && !repassword.equals("")) {
			if (!password.equals(repassword)) {
				message = "Xác thực mật khẩu không đúng ";
			} else {
				if (dao.findByUsername(username) == null) {
					if (password.length() < 6 || password.length() > 20) {
						message = "Mật khẩu từ 6 đến 20 kí tự!";
					} else {
						try {
							ac.setAddress("");
							ac.setCccd("");
							ac.setRole(new String[] { "USER" });
							ac.setImage("");
							ac.setPhone("");
							ac.setGender(false);
							ac.setFullname("");
							ac.setPassword(password);
							ac.setUsername(username);
							dao.create(ac);
							message = "Đăng kí thành công!";
							response.setSuccess(true);
						} catch (Exception e) {
							message = "Đăng kí thất bại!";
							e.printStackTrace();
						}
					}
				} else {
					message = "Tài khoản đã tồn tại!";
				}
			}
		} else {
			message = "Vui lòng nhập đầy đủ thông tin!";
		}
		response.setMessage(message);
		return ResponseEntity.ok(response);
	}

	private String currentUsernameForgotPassword = "";

	@RequestMapping("/account/retrieve-password")
	@ResponseBody
	public ResponseEntity<LoginResponse> retrievePassword(Model model, @RequestParam("username") String email) {
		LoginResponse response = new LoginResponse();
		String message = "";
		response.setSuccess(false);
		if (email != null || email != "") {
			try {
				Account ac = dao.findByUsername(email);
				if (ac != null) {
					currentUsernameForgotPassword = email;
					MailInformation mail = new MailInformation();
					mail.setTo(ac.getUsername());
					mail.setSubject("Quên mật khẩu");
					String verifyCode = String.valueOf(passwordUtil.generatePassword(6));
					session.setAttribute("verificationCode", verifyCode);
					session.setAttribute("verificationCodeExpiresAt", System.currentTimeMillis() + (30 * 1000));
					mail.setBody("<html><body>" + "<p>Xin chào " + ac.getUsername() + ",</p>"
							+ "<p>Chúng tôi nhận được yêu cầu thiết lập lại mật khẩu cho tài khoản HOTEL của bạn.</p>"
							+ "<p>Vui lòng không chia sẽ mã này cho bất cứ ai:" + "<h3>" + verifyCode + "</h3>" + "</p>"
							+ "<p>Nếu bạn không yêu cầu thiết lập lại mật khẩu, vui lòng liên hệ Bộ phận Chăm sóc Khách hàng tại đây</p>"
							+ "<p>Trân trọng,</p>" + "<p>Đội ngũ HOTEL</p>"
							+ "<p>Bạn có thắc mắc? Liên hệ chúng tôi tại đây andnpk02691@fpt.edu.vn.</p>"
							+ "</body></html>");
					mailServiceImplement.send(mail);
					message = "Một mã xác nhận đã được gửi đến email của bạn!";
					response.setSuccess(true);
				} else {
					message = "Tài khoản không tồn tại!";
				}
			} catch (Exception e) {
				e.printStackTrace();
				message = "Có lỗi xảy ra!";
			}
		}
		response.setMessage(message);
		return ResponseEntity.ok(response);
	}

	@RequestMapping("/account/code-retrieve-password")
	@ResponseBody
	public ResponseEntity<LoginResponse> submitNewPassword(Model model, @RequestParam("verifyCode") String verifyCode) {
		LoginResponse response = new LoginResponse();
		String message = "";
		response.setSuccess(false);
		String code = (String) session.getAttribute("verificationCode");
		long verificationCodeExpiresAt = 0;
		if (session.getAttribute("verificationCodeExpiresAt") != null) {
			verificationCodeExpiresAt = (long) session.getAttribute("verificationCodeExpiresAt");
		}
		if ((String) session.getAttribute("verificationCode") != ""
				|| (String) session.getAttribute("verificationCode") != null) {
			long currentTime = System.currentTimeMillis();
			if (!verifyCode.equals(code)) {
				message = "Mã xác nhận không đúng, vui lòng kiểm tra lại!";
			} else {
				if (verificationCodeExpiresAt < currentTime) {
					session.removeAttribute("verificationCode");
					session.removeAttribute("verificationCodeExpiresAt");
					message = "Mã Code đã hết hiệu lực!";
				} else {
					message = "Mã xác nhận hợp lệ!";
					response.setSuccess(true);
				}
			}
		} else {
			message = "Vui lòng lấy mã trước khi sang bước tiếp theo!";
		}
		response.setMessage(message);
		return ResponseEntity.ok(response);
	}

	@RequestMapping("/account/accept-change-password")
	public String acceptChangePassword() {
		return "user/forgot-password-finally";
	}

	@RequestMapping("/account/submit-retrieve-password")
	@ResponseBody
	public ResponseEntity<LoginResponse> RetrieveChange(Model model, @RequestParam("password") String newPass,
			@RequestParam("repassword") String rePass) {
		LoginResponse response = new LoginResponse();
		String message = "";
		response.setSuccess(false);
		if (newPass == "" || rePass == "") {
			message = "Vui lòng nhập đầy đủ thông tin!";
		} else {
			if (newPass.length() < 6 || newPass.length() > 20) {
				message = "Password từ 6 đến 20 kí tự!";
			} else {
				try {
					if (!newPass.equals(rePass)) {
						message = "Xác nhận mật khẩu chưa chính xác!";
					} else {
						Account ac = dao.findByUsername(currentUsernameForgotPassword);
						String key = dao.findKeyByUsername(currentUsernameForgotPassword);
						ac.setPassword(newPass);
						dao.update(key, ac);
						message = "Đổi mật khẩu thành công!";
						response.setSuccess(true);
					}
				} catch (Exception e) {
					message = "Đổi mật khẩu thất bại!";
				}
			}
		}
		response.setMessage(message);
		return ResponseEntity.ok(response);
	}

	@RequestMapping("/auth/logoff/success")
	public String logout_success(Model model) {
		model.addAttribute("message", "Đăng xuất thành công");
		return "redirect:/sign-in";
	}

	@RequestMapping("/auth/logoff/error")
	public String logout_error(Model model) {
		model.addAttribute("message", "Đăng xuất thất bại");
		return "redirect:/sign-in";
	}

	@RequestMapping("/auth/access/denied")
	public String denied(Model model) {
		System.out.println("lỗi");
		return "redirect:/";
	}

	@RequestMapping("/oauth2/login/success")
	public String googleSucces(OAuth2AuthenticationToken oauth2) {
		service.loginFromOAuth2(oauth2);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		System.out.println("tên " + auth.getPrincipal().toString());
		return "redirect:/";
	}

	@RequestMapping("/oauth2/login/error")
	public String googleError(Model model) {
		model.addAttribute("message", "Đăng nhập thất bại");
		return "redirect:/sign-in";
	}

	// INFO-User
	@GetMapping("/info-user")
	public String infoUser(Model model) {
		String username = (String) session.getAttribute("username");
		Account list = dao.findByUsername(username);
		String fullname = list.getFullname();
		String cccd = list.getCccd();
		boolean gender = list.isGender();
		String phone = list.getPhone();
		String address = list.getAddress();
		String image = list.getImage();
		Account account = new Account();
		account.setUsername(username);
		account.setFullname(fullname);
		account.setCccd(cccd);
		account.setGender(gender);
		account.setPhone(phone);
		account.setAddress(address);
		account.setImage(image);
		model.addAttribute("form", account);
		return "user/info-user";
	}

	@PostMapping("/info-user/update")
	@ResponseBody
	public boolean UpdateInfoUser(Model model, @RequestParam("fullname") String fullname,
			@RequestParam("cccd") String cccd, @RequestParam("phone") String phone,
			@RequestParam("address") String address, @RequestParam("image") String image,
			@RequestParam("gender") boolean gender) {
		String key = dao.findKeyByUsername((String) session.getAttribute("username"));
		Account account = dao.findByKey(key);
		try {
			account.setAddress(address);
			account.setPhone(phone);
			account.setFullname(fullname);
			account.setImage(image);
			account.setCccd(cccd);
			account.setGender(gender);
			dao.update(key, account);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}