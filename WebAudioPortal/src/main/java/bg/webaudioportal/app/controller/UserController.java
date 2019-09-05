package bg.webaudioportal.app.controller;

import java.io.File;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import bg.webaudioportal.app.model.Audio;
import bg.webaudioportal.app.model.User;
import bg.webaudioportal.app.repository.AudioRepository;
import bg.webaudioportal.app.repository.UserRepository;
import bg.webaudioportal.app.service.UserService;

@Controller
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private AudioRepository audioRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	private static String UPLOAD_FOLDER = "Uploads/";
	
	@RequestMapping(value = {"/", "/login"}, method = RequestMethod.GET)
	public ModelAndView login() {
		ModelAndView model = new ModelAndView();
		model.setViewName("user/login");
		return model;
	}
	
	@RequestMapping(value = {"/register"}, method = RequestMethod.GET)
	public ModelAndView register() {
		ModelAndView model = new ModelAndView();
		User user = new User();
		model.addObject("user", user);
		model.setViewName("user/register");
		return model;
	}
	
	@RequestMapping(value = {"/register"}, method = RequestMethod.POST)
	public ModelAndView createUser(@Valid User user, BindingResult bindingResult) {
		ModelAndView model = new ModelAndView();
		
		if (!isEmailValid(user.getEmail())) {
			bindingResult.rejectValue("email", "error.user", "This email is not valid!");
		}
		
		User userExists = userService.findUserByEmail(user.getEmail());
		
		if (userExists != null) {
			bindingResult.rejectValue("email", "error.user", "This email already exists!");
		}
		
		if (bindingResult.hasErrors()) {
			model.setViewName("user/register");
		}else {
			userService.saveUser(user);
			model.addObject("msg", "User has been registered successfully!");
			model.addObject("user", new User());
			model.setViewName("user/register");
		}
		return model;
	}
	
	@RequestMapping(value = {"/home"}, method = RequestMethod.GET)
	public ModelAndView home() {
		ModelAndView model = new ModelAndView();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
		if (user != null) {
			model.addObject("userName", user.getFirstname() + " " + user.getLastname());
		}
		model.setViewName("home/home");
		return model;
	}
	
	@RequestMapping(value = {"/access_denied"}, method = RequestMethod.GET)
	public ModelAndView accessDenied() {
		ModelAndView model = new ModelAndView();
		model.setViewName("error/access_denied");
		return model;
	}
	
	@RequestMapping(value = {"/my_profile"}, method = RequestMethod.GET)
	public ModelAndView myProfile() {
		ModelAndView model = new ModelAndView();
		String email = "";
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetails) {
			email = ((UserDetails)principal).getUsername();
		} else {
			email = principal.toString();
		}
		User user = userService.findUserByEmail(email);
		model.addObject("user", user);
		model.setViewName("user/my_profile");
		return model;
	}
	
	@RequestMapping("/user_profile/{email}")
	public ModelAndView userProfile(@PathVariable("email") String email) {
		ModelAndView model = new ModelAndView();
		User user = userService.findUserByEmail(email);
		model.addObject("user", user);
		String currentAccount = "";
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetails) {
			currentAccount = ((UserDetails)principal).getUsername();
		} else {
			currentAccount = principal.toString();
		}
		if (currentAccount.equals(user.getEmail())) {
			model.setViewName("user/my_profile");
		} else {
			model.setViewName("user/user_profile");
		}
		return model;
	}
	
	@RequestMapping(value = {"/all_users"}, method = RequestMethod.GET)
	public ModelAndView allUsers() {
		ModelAndView model = new ModelAndView();
		List<User> users = userRepository.findAll();
		model.addObject("users", users);
		model.setViewName("user/all_users");
		return model;
	}
	
	@GetMapping("/edit_profile")
	public String editProfile(Model model) {
		model.addAttribute("formUser", new User());
		return "user/edit_profile";
	}
	
	@PostMapping("/edit_profile")
	public RedirectView editProfile(@ModelAttribute User formUser, RedirectAttributes redirectAttributes) {
		String editMsg = "";
		boolean edited = false;
		boolean emailTaken = false;
		if ((formUser.getFirstname().isEmpty()) && (formUser.getLastname().isEmpty()) && (formUser.getEmail().isEmpty()) && (formUser.getPassword().isEmpty())) {
			editMsg = "Please input at least one value!";
			redirectAttributes.addFlashAttribute("actionMsg", editMsg);
			return new RedirectView("/edit_profile");
		}
		if ((!formUser.getFirstname().isEmpty()) || (!formUser.getLastname().isEmpty()) || (!formUser.getEmail().isEmpty()) || (!formUser.getPassword().isEmpty())) {
			editMsg = "You successfully changed the values of the following fields: ";
		}
		String currentAccount = "";
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetails) {
			currentAccount = ((UserDetails)principal).getUsername();
		} else {
			currentAccount = principal.toString();
		}
		User user = userService.findUserByEmail(currentAccount);
		if ((!formUser.getFirstname().isEmpty()) && (!formUser.getFirstname().equals(user.getFirstname()))) {
			user.setFirstname(formUser.getFirstname());
			editMsg += " Firstname";
			edited = true;
		}
		if ((!formUser.getLastname().isEmpty()) && (!formUser.getLastname().equals(user.getLastname()))) {
			user.setLastname(formUser.getLastname());
			editMsg += " Lastname";
			edited = true;
		}
		if ((!formUser.getPassword().isEmpty()) && (!formUser.getPassword().equals(user.getPassword()))) {
			user.setPassword(bCryptPasswordEncoder.encode(formUser.getPassword()));
			editMsg += " Password";
			edited = true;
		}
		if (edited == true) {
			userRepository.save(user);
		}
		if ((!formUser.getEmail().isEmpty()) && (!formUser.getEmail().equals(user.getEmail()))) {
			List<User> allUsers = userRepository.findAll();
			for (User usr : allUsers) {
				if (usr.getEmail().equals(formUser.getEmail())) {
					emailTaken = true;
					break;
				}
			}
			if (emailTaken == false) {
				List<Audio> allAudioFiles = audioRepository.findAll();
				for (Audio audio : allAudioFiles) {
					if (audio.getUploader().equals(user.getEmail())) {
						audio.setUploader(formUser.getEmail());
						audioRepository.save(audio);
					}
				}
				user.setEmail(formUser.getEmail());
				userRepository.save(user);
				return new RedirectView("/logout");
			}
		}
		redirectAttributes.addFlashAttribute("actionMsg", editMsg);
		return new RedirectView("/edit_profile");
	}
	
	@GetMapping("/toggle_account")
	public RedirectView toggleAccount(RedirectAttributes redirectAttributes) {
		String msg = "";
		String currentAccount = "";
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetails) {
			currentAccount = ((UserDetails)principal).getUsername();
		} else {
			currentAccount = principal.toString();
		}
		User user = userService.findUserByEmail(currentAccount);
		if (user.getActive() == 1) {
			user.setActive(0);
			msg = "You successfully deactivated your account!";
		} else {
			user.setActive(1);
			msg = "You successfully activated your account!";
		}
		userRepository.save(user);
		redirectAttributes.addFlashAttribute("actionMsg", msg);
		return new RedirectView("/edit_profile");
	}
	
	@GetMapping("/delete_all")
	public RedirectView deleteAllAudioFiles(RedirectAttributes redirectAttributes) {
		String msg = "";
		boolean deleted = false;
		String currentAccount = "";
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetails) {
			currentAccount = ((UserDetails)principal).getUsername();
		} else {
			currentAccount = principal.toString();
		}
		List<Audio> allAudioFiles = audioRepository.findAll();
		for (Audio audio : allAudioFiles) {
			if (audio.getUploader().equals(currentAccount)) {
				String extension = audio.getPath().substring(audio.getPath().lastIndexOf(".") + 1);
				File file = new File(UPLOAD_FOLDER + audio.getId() + "." + extension);
				if (file.exists()) {
					userService.deleteAudio(audio);
					deleted = file.delete();
				}
			}
		}
		if (deleted) {
			msg = "You successfully deleted all of your audio files!";
		} else {
			msg = "You have no audio files to delete!";
		}
		redirectAttributes.addFlashAttribute("actionMsg", msg);
		return new RedirectView("/edit_profile");
	}
	
	@RequestMapping(value = {"/resources"}, method = RequestMethod.GET)
	public ModelAndView usedResources() {
		ModelAndView model = new ModelAndView();
		model.setViewName("home/resources");
		return model;
	}
	
	@RequestMapping(value = {"/shoutbox"}, method = RequestMethod.GET)
	public ModelAndView showShoutbox() {
		ModelAndView model = new ModelAndView();
		model.setViewName("home/shoutbox");
		return model;
	}
	
	static boolean isEmailValid(String email) {
	      String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
	      return email.matches(regex);
	   }
}