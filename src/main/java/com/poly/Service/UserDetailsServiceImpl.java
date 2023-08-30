package com.poly.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import com.poly.Bean.Account;
import com.poly.DAO.AccountDAO;

import jakarta.servlet.http.HttpSession;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private AccountDAO accountDAO;
	@Autowired
	HttpSession session;
	
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Account account = accountDAO.findByUsername(username);
		if (account == null) {
			throw new UsernameNotFoundException("Không tìm thấy tài khoản với username: " + username);
		} else {
			List<GrantedAuthority> authorities = new ArrayList<>();
			for (String role : account.getRole()) {
				authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
			}
			return new User(account.getUsername(), passwordEncoder().encode(account.getPassword()), authorities);
		}
	}

	public void loginFromOAuth2(OAuth2AuthenticationToken oauth2) {
		String email = oauth2.getPrincipal().getAttribute("email");
		String name = oauth2.getPrincipal().getAttribute("name");
		String password = Long.toHexString(System.currentTimeMillis());
		session.setAttribute("username", email);
		session.setAttribute("admin", false);
		UserDetails user = User.withUsername(email).password(passwordEncoder().encode(password)).roles("GUEST").build();
		Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(auth);
	}
}
