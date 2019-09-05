package bg.webaudioportal.app.controller;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import bg.webaudioportal.app.model.Audio;
import bg.webaudioportal.app.model.User;
import bg.webaudioportal.app.repository.AudioRepository;
import bg.webaudioportal.app.service.UserService;

@RestController
@RequestMapping("/deleteMine")
public class DeleteMineController {
	
	private static String UPLOAD_FOLDER = "Uploads/";
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private AudioRepository audioRepository;
	
	@RequestMapping("/{fileName:.+}")
	public RedirectView  deleteAudioResource(HttpServletRequest request, HttpServletResponse response, @PathVariable("fileName") String fileName, RedirectAttributes redirectAttributes) throws IOException {
		String deleteMsg = "";
		String currentAccount = "";
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetails) {
			currentAccount = ((UserDetails)principal).getUsername();
		} else {
			currentAccount = principal.toString();
		}
		User user = userService.findUserByEmail(currentAccount);
		if (user != null) {
			if (user.getActive() == 0) {
				deleteMsg = "You cannot delete audio files until you activate your account!";
			} else {
				File file = new File(UPLOAD_FOLDER + fileName);
				if (file.exists()) {
					int id = Integer.valueOf(removeExtension(file.getName()));
					Audio audio = userService.findAudioById(id);
					String name = audio.getName();
					String uploader = audio.getUploader();
					if (currentAccount.equals(uploader)) {
						userService.deleteAudio(audio);
						boolean deleted = file.delete();
						if (deleted) {
							deleteMsg = "You successfully deleted audio '" + name + "'";
						}
					} else {
						deleteMsg = "Only " + uploader + " can delete their audio files!";
					}
				}
				Iterable<Audio> collection = audioRepository.findAll();
				redirectAttributes.addFlashAttribute("allAudioFiles", collection);
				int count = 0;
				for (Audio audio : collection) {
					if (audio.isActive()) {
						count++;
					}
				}
				redirectAttributes.addFlashAttribute("allAudioFilesCount", count);
			}
		} else {
			deleteMsg = "Please log in to be able to delete audio files!";
		}
		redirectAttributes.addFlashAttribute("actionMsg", deleteMsg);
		return new RedirectView("/display_my");
	}
	
	public static String removeExtension(String fileName) {
        if (fileName.indexOf(".") > 0) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        } else {
            return fileName;
        }
    }
}