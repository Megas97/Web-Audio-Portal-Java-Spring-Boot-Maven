package bg.webaudioportal.app.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import bg.webaudioportal.app.model.Audio;
import bg.webaudioportal.app.model.Role;
import bg.webaudioportal.app.model.User;
import bg.webaudioportal.app.repository.AudioRepository;
import bg.webaudioportal.app.repository.RoleRepository;
import bg.webaudioportal.app.repository.UserRepository;

@Service("userService")
public class UserServiceImpl implements UserService {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private AudioRepository audioRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Override
	public User findUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}
	
	@Override
	public List<Audio> findAudioByUploader(String uploader) {
		return audioRepository.findAudioByUploader(uploader);
	}
	
	@Override
	public void saveUser(User user) {
		user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		user.setActive(1);
		Role userRole = roleRepository.findByRole("USER");
		user.setRoles(new HashSet<Role>(Arrays.asList(userRole)));
		userRepository.save(user);
	}
	
	@Override
	public void saveAudio(Audio audio) {
		audioRepository.save(audio);
	}
	
	@Override
	public Audio findAudioById(int id) {
		return audioRepository.findById(id);
	}
	
	@Override
	public void deleteAudio(Audio audio) {
		audioRepository.delete(audio);
	}
}