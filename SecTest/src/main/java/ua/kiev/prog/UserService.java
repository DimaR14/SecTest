package ua.kiev.prog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CustomUser> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CustomUser findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    @Transactional
    public void deleteUsers(List<Long> ids) {
        ids.forEach(id -> {
            Optional<CustomUser> user = userRepository.findById(id);
            user.ifPresent(u -> {
                if ( ! AppConfig.ADMIN.equals(u.getLogin())) {
                    userRepository.deleteById(u.getId());
                }
            });
        });
    }

    @Transactional
    public boolean addUser(String login, String passHash,
                           UserRole role, String email,
                           String phone) {
        if (userRepository.existsByLogin(login))
            return false;

        CustomUser user = new CustomUser(login, passHash, role, email, phone);
        userRepository.save(user);

        return true;
    }

    @Transactional
    public void updateUser(String login, String email, String phone) {
        CustomUser user = userRepository.findByLogin(login);
        if (user == null)
            return;

        user.setEmail(email);
        user.setPhone(phone);

        userRepository.save(user);
    }

    @Transactional
    public void newPassword(String login, String password){
       CustomUser user = userRepository.findByLogin(login);
       if(user == null)
           return;

       user.setPassword(password);
       userRepository.save(user);
    }
}
