package ua.kiev.prog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.MimeMappings;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

@Controller
public class MyController {

    private  String log = "log";

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @RequestMapping("/")
    public String index(Model model) {
        User user = getCurrentUser();

        String login = user.getUsername();
        CustomUser dbUser = userService.findByLogin(login);

        model.addAttribute("login", login);
        model.addAttribute("roles", user.getAuthorities());
        model.addAttribute("admin", isAdmin(user));
        model.addAttribute("email", dbUser.getEmail());
        model.addAttribute("phone", dbUser.getPhone());

        return "index";
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public String update(@RequestParam(required = false) String email,
                         @RequestParam(required = false) String phone) {
        User user = getCurrentUser();

        String login = user.getUsername();
        userService.updateUser(login, email, phone);

        return "redirect:/";
    }

    @RequestMapping(value = "/newuser", method = RequestMethod.POST)
    public String update(@RequestParam String login,
                         @RequestParam String password,
                         @RequestParam(required = false) String email,
                         @RequestParam(required = false) String phone,
                         Model model) {
        String passHash = passwordEncoder.encode(password);

        if (!userService.addUser(login, passHash, UserRole.USER, email, phone)) {
            model.addAttribute("exists", true);
            model.addAttribute("login", login);
            return "register";
        }

        return "redirect:/";
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public String delete(@RequestParam(name = "toDelete[]", required = false) List<Long> ids,
                         Model model) {
        userService.deleteUsers(ids);
        model.addAttribute("users", userService.getAllUsers());

        return "admin";
    }

    public static void mas(String mail) throws IOException, MessagingException {
        final Properties properties = new Properties();
        properties.load(MyController.class.getClassLoader().getResourceAsStream("application.properties"));

        Session mailSession = Session.getDefaultInstance(properties);
        MimeMessage message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress("Home.work.104410@gmail.com"));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(mail));
        message.setSubject("password");
        message.setText("http://localhost:8078/new_password");

        Transport tr = mailSession.getTransport();
        tr.connect(null, "homework104410!");
        tr.sendMessage(message, message.getAllRecipients());
        tr.close();
    }

    @RequestMapping(value = "/forgot", method = RequestMethod.POST)
    public String forgotPassword(@RequestParam String login) throws IOException, MessagingException {
        CustomUser user = userService.findByLogin(login);
        this.log = user.getLogin();
        if (user.getEmail().equals("")) {
            return "new_mail";
        }
        mas(user.getEmail());
        return "check_mail";
    }

    @RequestMapping(value = "/mail", method = RequestMethod.POST)
    public String newMail(@RequestParam String mail) throws IOException, MessagingException {
        String log1 = log;
        userService.newMail(log1, mail);
        mas(mail);
        return "check_mail";
    }

    @RequestMapping(value = "/newpassword", method = RequestMethod.POST)
    public String newPassword(@RequestParam String password) {
        List<CustomUser> list = userService.getAllUsers();
        List<String> loglist = new ArrayList<>();
        for (CustomUser u : list) {
            loglist.add(u.getLogin());
        }
        int x = 0;
        for (String log1 : loglist) {
            if (log1.equals(log)) {
                x = x + 1;
            }
        }
        if (x == 0) {
            return "redirect:/login?error";
        }
        String passHash = passwordEncoder.encode(password);
        CustomUser user = userService.findByLogin(log);
        userService.newPassword(user.getLogin(), passHash);
        return "redirect:/";
    }

    @RequestMapping("/login")
    public String loginPage() {
        return "login";
    }

    @RequestMapping("/check_mail")
    public String checkMailPage() {
        return "check_mail";
    }

    @RequestMapping("/forgot")
    public String forgotPage() {
        return "forgot";
    }

    @RequestMapping("new_mail")
    public String newMailPage() {
        return "new_mail";
    }

    @RequestMapping("/new_password")
    public String newPasswordPage() {
        return "new_password";
    }

    @RequestMapping("/register")
    public String register() {
        return "register";
    }

    @RequestMapping("/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')") // !!!
    public String admin(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin";
    }

    @RequestMapping("/unauthorized")
    public String unauthorized(Model model) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        model.addAttribute("login", user.getUsername());
        return "unauthorized";
    }

    // ----

    private User getCurrentUser() {
        return (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }


    private boolean isAdmin(User user) {
        Collection<GrantedAuthority> roles = user.getAuthorities();

        for (GrantedAuthority auth : roles) {
            if ("ROLE_ADMIN".equals(auth.getAuthority()))
                return true;
        }

        return false;
    }
}
