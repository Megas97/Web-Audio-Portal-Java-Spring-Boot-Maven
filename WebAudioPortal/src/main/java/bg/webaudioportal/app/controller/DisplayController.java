package bg.webaudioportal.app.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import bg.webaudioportal.app.model.Audio;
import bg.webaudioportal.app.repository.AudioRepository;

@Controller
public class DisplayController {
	
	@Autowired
	private AudioRepository audioRepository;
	
	@RequestMapping(value = {"/display_all"}, method = RequestMethod.GET)
	public ModelAndView displayAll() {
		ModelAndView model = new ModelAndView();
		Iterable<Audio> collection = audioRepository.findAll();
		model.addObject("allAudioFiles", collection);
		int count = 0;
		for (Audio audio : collection) {
			if (audio.isActive()) {
				count++;
			}
		}
		String countString = "";
		if (count == 1) {
			countString = "There is " + count + " audio file available!";
		} else if (count > 1) {
			countString = "There are " + count + " audio files available!";
		} else {
			countString = "There are no audio files available!";
		}
		model.addObject("allAudioFilesCountString", countString);
		model.setViewName("display/display_all");
		return model;
    }
	
	@RequestMapping(value = {"/display_my"}, method = RequestMethod.GET)
	public ModelAndView displayMy() {
		ModelAndView model = new ModelAndView();
		String currentAccount = "";
		int totalCount = 0;
		int publicCount = 0;
		int privateCount = 0;
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetails) {
			currentAccount = ((UserDetails)principal).getUsername();
		} else {
			currentAccount = principal.toString();
		}
		List<Audio> myUploads = audioRepository.findAudioByUploader(currentAccount);
		totalCount = myUploads.size();
		String totalCountString = "";
		if (totalCount == 1) {
			totalCountString = "You have " + totalCount + " uploaded audio file!";
		} else if (totalCount > 1) {
			totalCountString = "You have " + totalCount + " uploaded audio files!";
		} else {
			totalCountString = "You have no uploaded audio files!";
		}
		model.addObject("totalAudioFilesCountString", totalCountString);
		List<Audio> publicAudioFiles = new ArrayList<Audio>();
		List<Audio> privateAudioFiles = new ArrayList<Audio>();
		for (Audio audio : myUploads) {
			if (audio.isActive()) {
				publicAudioFiles.add(audio);
			} else {
				privateAudioFiles.add(audio);
			}
		}
		model.addObject("publicAudioFiles", publicAudioFiles);
		model.addObject("privateAudioFiles", privateAudioFiles);
		for (Audio audio : myUploads) {
			if (audio.isActive()) {
				publicCount++;
			} else {
				privateCount++;
			}
		}
		model.addObject("publicAudioFilesCount", publicCount);
		model.addObject("privateAudioFilesCount", privateCount);
		model.setViewName("display/display_my");
		return model;
    }
	
	@RequestMapping("/display_user/{email}")
	public ModelAndView displayUser(@PathVariable("email") String email) {
		ModelAndView model = new ModelAndView();
		model.addObject("email", email);
		String currentAccount = "";
		int totalCount = 0;
		int publicCount = 0;
		int privateCount = 0;
		List<Audio> userUploads = audioRepository.findAudioByUploader(email);
		totalCount = userUploads.size();
		String totalCountString = "";
		if (totalCount == 1) {
			totalCountString = email + " has " + totalCount + " uploaded audio file!";
		} else if (totalCount > 1) {
			totalCountString = email + " has " + totalCount + " uploaded audio files!";
		} else {
			totalCountString = email + " has no uploaded audio files!";
		}
		model.addObject("totalAudioFilesCountString", totalCountString);
		List<Audio> publicAudioFiles = new ArrayList<Audio>();
		List<Audio> privateAudioFiles = new ArrayList<Audio>();
		for (Audio audio : userUploads) {
			if (audio.isActive()) {
				publicAudioFiles.add(audio);
			} else {
				privateAudioFiles.add(audio);
			}
		}
		model.addObject("publicAudioFiles", publicAudioFiles);
		model.addObject("privateAudioFiles", privateAudioFiles);
		for (Audio audio : userUploads) {
			if (audio.isActive()) {
				publicCount++;
			} else {
				privateCount++;
			}
		}
		model.addObject("publicAudioFilesCount", publicCount);
		model.addObject("privateAudioFilesCount", privateCount);
		if (currentAccount.equals(email)) {
			model.setViewName("display/display_my");
		} else {
			model.setViewName("display/display_user");
		}
		return model;
    }
}