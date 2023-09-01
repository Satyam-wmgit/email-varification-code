package com.example.registerapp.service;

import com.example.registerapp.dto.LoginDto;
import com.example.registerapp.dto.RegisterDto;
import com.example.registerapp.entity.User;
import com.example.registerapp.repository.UserRepository;
import com.example.registerapp.util.EmailUtil;
import com.example.registerapp.util.OtpUtil;
import jakarta.mail.MessagingException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  @Autowired
  private OtpUtil otpUtil;
  @Autowired
  private EmailUtil emailUtil;
  @Autowired
  private UserRepository userRepository;

  public String register(RegisterDto registerDto) {
	Optional<User> users=userRepository.findByEmail(registerDto.getEmail())	;
	 if(users.isPresent()) {
		 String mail=users.get().getEmail();
		if(mail.equals(registerDto.getEmail())) {
			System.out.println(mail.equals(registerDto.getEmail()));
		
			return" user already registered";
		}
	 }
		 
    String otp = otpUtil.generateOtp();
    try {
      emailUtil.sendOtpEmail(registerDto.getEmail(), otp);
    } catch (MessagingException e) {
      throw new RuntimeException("Unable to send otp please try again");
    }
    User user = new User();
    user.setName(registerDto.getName());
    user.setEmail(registerDto.getEmail());
    user.setPassword(registerDto.getPassword());
    user.setOtp(otp);
    user.setOtpGeneratedTime(LocalDateTime.now());
    userRepository.save(user);
   // return "User registration successful";
    return "Email sent... please verify account within 1 minute";
  }

  public String verifyAccount(String email, String otp) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found with this email: " + email));
    if (user.getOtp().equals(otp) && Duration.between(user.getOtpGeneratedTime(),
        LocalDateTime.now()).getSeconds() < (1 * 60)) {
      user.setActive(true);
      userRepository.save(user);
      return "OTP verified you can login";
    }
    else if(!user.getOtp().equals(otp))
    	return "OTp is incorrect";
    	
    return "Please regenerate otp and try again";
  }

  public String regenerateOtp(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found with this email: " + email));
    String otp = otpUtil.generateOtp();
    try {
      emailUtil.sendOtpEmail(email, otp);
    } catch (MessagingException e) {
      throw new RuntimeException("Unable to send otp please try again");
    }
    user.setOtp(otp);
    user.setOtpGeneratedTime(LocalDateTime.now());
    userRepository.save(user);
    return "Email sent... please verify account within 1 minute";
  }

  public String login(LoginDto loginDto) {
//    User user = userRepository.findByEmail(loginDto.getEmail())
//        .orElseThrow(
//            () -> new RuntimeException("User not found with this email: " + loginDto.getEmail()));
//    
	  Optional<User> user = userRepository.findByEmail(loginDto.getEmail());
	  if(user.isEmpty()) {
		  return "user not found with this email";
	  }
    if (!loginDto.getPassword().equals(user.get().getPassword())) {
      return "Password is incorrect";
    } else if (!user.get().isActive()) {
      return "your account is not verified";
    }
    return "Login successful";
  }
}
