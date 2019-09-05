package bg.webaudioportal.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import bg.webaudioportal.app.model.Audio;
import bg.webaudioportal.app.model.User;
import bg.webaudioportal.app.service.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class UploadController {
	
	@Autowired
	private UserService userService;
	
    private static String UPLOAD_FOLDER = "Uploads/";
    
    @GetMapping("/upload")
    public String index() {
        return "upload/upload";
    }
    
    @PostMapping("/upload")
    public String fileUpload(@RequestParam("file") MultipartFile[] files, RedirectAttributes redirectAttributes) {
        String msg = "You successfully uploaded: " + System.lineSeparator();
        long totalSize = 0;
        try {
        	if (files.length == 1) {
        		if (!files[0].isEmpty()) {
        			String ext = files[0].getOriginalFilename().substring(files[0].getOriginalFilename().lastIndexOf(".") + 1);
            		if (!ext.equals("mp3")) {
            			msg = "Only .mp3 audio files are allowed!";
            			redirectAttributes.addFlashAttribute("message", msg);
                		return "redirect:/upload";
            		} else {
            			totalSize += files[0].getSize();
            			if (totalSize > 104857600) { // If total files size is bigger than 100 MB
            				msg = "Maximum total files size is 100 MB!";
            				redirectAttributes.addFlashAttribute("message", msg);
                    		return "redirect:/upload";
            			}
            		}
        		} else {
        			msg = "Please select files to upload!";
        			redirectAttributes.addFlashAttribute("message", msg);
            		return "redirect:/upload";
        		}
        	}
        	for (MultipartFile file : files) {
        		totalSize += file.getSize();
        	}
        	if (totalSize > 104857600) { // If total files size is bigger than 100 MB
				msg = "Maximum total files size is 100 MB!";
				redirectAttributes.addFlashAttribute("message", msg);
        		return "redirect:/upload";
			}
        	for (MultipartFile file : files) {
        		String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        		if (ext.equals("mp3")) {
        			totalSize += file.getSize();
        			String uploader = "";
            		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            		if (principal instanceof UserDetails) {
            			uploader = ((UserDetails)principal).getUsername();
            		} else {
            			uploader = principal.toString();
            		}
            		User user = userService.findUserByEmail(uploader);
            		if (user.getActive() == 0) {
            			msg = "You cannot upload audio files until you activate your account!";
            		} else {
            			Audio audio = new Audio();
                		audio.setName("");
                		audio.setSize(0);
                		audio.setUploader("");
                		audio.setDate("");
                		audio.setPath("");
                		audio.setActive(false);
                		userService.saveAudio(audio);
                		int id = audio.getId();
                		audio.setName(file.getOriginalFilename().replaceFirst("[.][^.]+$", ""));
            			audio.setUploader(uploader);
                		double size = round(((file.getSize() / 1024f) / 1024f), 1); // In MB
                		audio.setSize(size);
                		DateFormat dateFormat = new SimpleDateFormat("E, dd MMMM yyyy");
                		Date now = new Date();
                		String date = dateFormat.format(now).toString();
                		audio.setDate(date);
                		String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
                		String path = UPLOAD_FOLDER + id + "." + extension;
                		audio.setPath(path);
                		audio.setActive(true);
                		userService.saveAudio(audio);
                		byte[] bytes = file.getBytes();
                		Path location = Paths.get(UPLOAD_FOLDER + id + "." + extension);
                		Files.write(location, bytes);
                		msg += file.getOriginalFilename() + System.lineSeparator();
            		}
        		}
        	}
        	redirectAttributes.addFlashAttribute("message", msg);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return "redirect:/upload";
    }
    
    private static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }
}