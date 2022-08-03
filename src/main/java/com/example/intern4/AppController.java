package com.example.intern4;

import com.example.intern4.model.User;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
public class AppController {

    @Autowired
    private UserRepository userRepository;

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");

    @GetMapping("/login")
    public String loginPage(Model model, String error, String logout){
        if (error != null)
            model.addAttribute("errorMsg", "Your username and password are invalid.");

        if (logout != null)
            model.addAttribute("msg", "You have been logged out successfully.");
        return "login";
    }

    @GetMapping("")
    public String viewHomePage(Authentication authentication){
        if(authentication != null && authentication.isAuthenticated()){
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            if(!(userRepository.findByEmail(customUserDetails.getUsername()) != null && userRepository.findByEmail(customUserDetails.getUsername()).isEnabled())){
                authentication.setAuthenticated(false);
                return "login";
            }
            Date date = new Date();
            userRepository.updateLastLoginDateById(customUserDetails.getUserId(),formatter.format(date));
            return "home";
        }
        return "index";
    }

    @GetMapping("/register")
    public String showSignUpForm(Model model){
        model.addAttribute("user", new User());
        return "signup_form";
    }

    @GetMapping("/list_users")
    public String viewUsersList(Authentication authentication,Model model){
        if(authentication != null && authentication.isAuthenticated()){
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            if(!(userRepository.findByEmail(customUserDetails.getUsername()) != null && userRepository.findByEmail(customUserDetails.getUsername()).isEnabled())){
                authentication.setAuthenticated(false);
                return "login";
            }
            List<User> userList = userRepository.findAll();
            model.addAttribute("listUser",userList);
            return "users";
        } else {
            return "login";
        }
    }

    @PostMapping("/block")
    public void blockUser(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(authentication != null && authentication.isAuthenticated()){
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            if(!(userRepository.findByEmail(customUserDetails.getUsername()) != null && userRepository.findByEmail(customUserDetails.getUsername()).isEnabled())){
                authentication.setAuthenticated(false);
                response.setContentType("text/html");
                response.getWriter().write("login");
                return;
            }
            String block_ids = request.getParameter("id");
            boolean flag = Boolean.parseBoolean(request.getParameter("flag"));
            boolean isUser=false;
            for (String id:block_ids.split(",")){
                if(customUserDetails.getUserId() == Long.valueOf(id)){
                    isUser = true;
                }
                userRepository.updateStatusById(Long.valueOf(id), flag);
            }
            if(isUser && !flag){
                userRepository.updateStatusById(customUserDetails.getUserId(),flag);
                response.setContentType("text/html");
                response.getWriter().write("login");
                return;
            }
            List<User> userList = userRepository.findAll();
            Gson gson = new Gson();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(gson.toJson(userList));
        } else {
            response.setContentType("text/html");
            response.getWriter().write("error");
        }
    }

    @PostMapping("/delete")
    public void deleteUser(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if(authentication != null && authentication.isAuthenticated()){
            String delete_ids = request.getParameter("id");
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            if(!(userRepository.findByEmail(customUserDetails.getUsername()) != null && userRepository.findByEmail(customUserDetails.getUsername()).isEnabled())){
                authentication.setAuthenticated(false);
                response.setContentType("text/html");
                response.getWriter().write("login");
                return;
            }
            for (String id:delete_ids.split(",")){
                userRepository.deleteById(Long.valueOf(id));
                if(customUserDetails.getUserId() == Long.valueOf(id)){
                    authentication.setAuthenticated(false);
                }
            }
            if(!authentication.isAuthenticated()){
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("login");
            } else {
                List<User> userList = userRepository.findAll();
                Gson gson = new Gson();
                response.getWriter().write(gson.toJson(userList));
            }
        } else {
            response.setContentType("text/html");
            response.getWriter().write("login");
        }
    }

    @PostMapping("/process_register")
    public String processRegistration(User user, Model model){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if(userRepository.findByEmail(user.getEmail()) != null){
            model.addAttribute("errorMsg1", "Email already exists !!!");
            return "signup_form";
        }
        user.setPassword(encoder.encode(user.getPassword()));
        user.setEnabled(true);
        Date date = new Date();
        user.setRegDate(formatter.format(date));
        user.setLastLoginDate(formatter.format(date));
        userRepository.save(user);
        return "register_success";
    }
}
