package ua.kiev.prog;

import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Controller
public class MyController {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @RequestMapping("/")
    public String index(Model model){
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

        if ( ! userService.addUser(login, passHash, UserRole.USER, email, phone)) {
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

    @RequestMapping(value = "/newpassword", method = RequestMethod.POST)
    public String newPassword(@RequestParam String login,
                                @RequestParam String password){
        List<CustomUser> list =userService.getAllUsers();
        List<String> loglist = new ArrayList<>();
        for(CustomUser u : list){
            loglist.add(u.getLogin());
        }
        int x = 0;
        for(String log : loglist){
            if(log.equals(login)){
                x= x+1;
            }
        }
        if(x==0){
            return "redirect:/login?error";
        }
        String passHash = passwordEncoder.encode(password);
        CustomUser user = userService.findByLogin(login);
        userService.newPassword(login,passHash);
        return "redirect:/";
    }

    @RequestMapping("/login")
    public String loginPage() {
        return "login";
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
    public String unauthorized(Model model){
        User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        model.addAttribute("login", user.getUsername());
        return "unauthorized";
    }

    // ----

    private User getCurrentUser() {
        return (User)SecurityContextHolder
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
