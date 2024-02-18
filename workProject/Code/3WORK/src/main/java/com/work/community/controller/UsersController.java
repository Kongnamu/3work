package com.work.community.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.work.community.config.SecurityUser;
import com.work.community.dto.CommentsDTO;
import com.work.community.dto.UsersDTO;
import com.work.community.entity.Users;
import com.work.community.repository.UsersRepository;
import com.work.community.service.CommentsService;
import com.work.community.service.UsersService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Controller
public class UsersController {
	
	private final UsersService usersService;
	
	private final UsersRepository usersRepository;
	
	private final CommentsService commentsService;
	
	//로그인 페이지 요청 :  /login
	@GetMapping("/login")
	public String loginForm() {
		return "login";  //login.html
	}
	
//	@RestController
//	public class KakaoLoginController {
//
//	    @Autowired
//	    private UsersService usersService;
//
//	    @PostMapping("/login")
//	    public ResponseEntity<?> handleKakaoLogin(@RequestBody UsersDTO usersDTO) {
//	        // 받아온 카카오 사용자 정보를 UserService를 통해 처리하고 데이터베이스에 저장
//	        usersService.saveKakaoUsers(usersDTO);
//	        return ResponseEntity.ok().build();
//	    }
//	}
//	//로그인 처리
//	@PostMapping("/login")
//	public String login(@ModelAttribute Users users, HttpSession session) {
//		Users loginUser = usersService.login(users);
//		if(loginUser != null
//				&& loginUser.getUpassword().equals(users.getUpassword())) {
//			//아이디 비번 일치해서 로그인 되면 세션 발급
//			session.setAttribute("sessionId", loginUser.getUid());
//			return "main";
//		}else {
//			return "login";
//		}
//	}
	
	
	//유저 홈페이지
    @GetMapping("/user/userpage/{uno}")
     public String userPage(@PathVariable Integer uno,
       @PageableDefault(page = 1) Pageable pageable,
                       Model model) {
       Page<CommentsDTO> commentList = commentsService.findByUno(pageable, uno);
       //방문자 수
       usersService.updateHits(uno);
       Users users = usersService.findById(uno);
       
       //하단에 페이지 영역 만들기
       int blockLimit = 10; //하단에 보여줄 페이지 개수
       //시작페이지
       int startPage = ((int)(Math.ceil((double)pageable.getPageNumber() / blockLimit)) - 1)* blockLimit + 1;
       //마지막 페이지
       int endPage = (startPage + blockLimit - 1) > commentList.getTotalPages() ?
            commentList.getTotalPages() : startPage + blockLimit - 1;
    
         model.addAttribute("commentList", commentList);
         System.out.println(commentList.toString());
         model.addAttribute("Userinfo", users);
         model.addAttribute("startPage", startPage);
         model.addAttribute("endPage", endPage);
       return "user/userpage";
    }
    
	//회원 가입 페이지
	@GetMapping("/user/join")
	public String joinForm(UsersDTO usersDTO) {
		return "user/join";
	}
	// 회원 가입 처리
	   // @Valid : 필드의 유효성 검사
	   // BindingResult: 에러 처리 클래스
	   @PostMapping("/user/join")
	   public String join(@Valid UsersDTO usersDTO, BindingResult bindingResult) {
	      if (bindingResult.hasErrors()) {
	         // 에러가 있으면 회원 가입 페이지에 머무름
	         return "user/join";
	      }
	      //방문자수 0으로 설정
	      usersDTO.setHits(0);
	      usersService.save(usersDTO);
	      return "user/join_wel";
	   }
	
	
		// 이메일 중복 검사
		@PostMapping("/user/check-email")
		public @ResponseBody String checkId(@RequestParam("uid") String uid) {
			String resultText = usersService.checkId(uid);
			return resultText; // res
		}

		// 닉네임 중복 검사
		@PostMapping("/user/check-nickname")
		public @ResponseBody String checkNickname(@RequestParam("unickname") String unickname) {
			String result = usersService.checkNickname(unickname);
			return result;
		}
			
		//로그아웃
		@GetMapping("/logout")
		public String logout() {
			return "redirect:/main";
		}
		

		//회원 목록
		@GetMapping("/user/list")
		public String getList(Model model) {
			List<UsersDTO> usersDTOList = usersService.findAll();
			model.addAttribute("usersList", usersDTOList);
			return "user/list";
		}
	
		//	//회원 상세 보기
		//	@GetMapping("/user/{uno}")
		//	public String getMember(@PathVariable Integer uno,
		//			Model model) {
		//		UsersDTO usersDTO = usersService.findById(uno);
		//		model.addAttribute("users",usersDTO);
		//		return "user/detail";
		//	}

		//회원 삭제
		@GetMapping("/user/delete/{uno}")
		public String deleteUsers(@PathVariable Integer uno) {
			usersService.deleteById(uno);
			return "redirect:/user/list";
		}
		
		// 아이디 찾기 페이지
		@GetMapping("/user/id_search")
		public String idSearchForm() {
		    return "user/id_search"; // 아이디 찾기 폼 페이지 반환
		}

		//아이디 찾기 ( 이름과전화번호)
		@PostMapping("/user/id_search")
		public String idSearch(@RequestParam("uname") String name, @RequestParam("uphone") String phone, Model model) {
		    Optional<Users> usersOptional = usersRepository.findByUnameAndUphone(name, phone);
		    if(usersOptional.isPresent()) {
		        Users users = usersOptional.get();
		        model.addAttribute("uId", users.getUid());
		        model.addAttribute("joinDate", users.getCreatedDate()); // 가입 날짜, BaseEntity에서 상속받은 createdAt 사용
		        return "/user/id_result"; // 결과 페이지로 이동
		    } else {
		        model.addAttribute("message", "일치하는 사용자 정보가 없습니다.");
		        return "/user/id_search"; // 정보가 없는 경우 다시 아이디 찾기 페이지로 이동
		    }
		}
		//main 창에서 유저 검색 
		@PostMapping("/user/search")
		public String searchUsers(@RequestParam("uid") String uid, Model model) {
		    List<UsersDTO> usersList = usersService.searchUsersByUid(uid);
		    model.addAttribute("usersList", usersList);
		    return "searchResult"; // 검색 결과를 표시할 페이지
		}
		
		// 비밀번호 찾기 페이지
		@GetMapping("/user/pw_search")
		public String pwSearchForm() {
		    return "user/pw_search"; // 비밀번호 찾기 폼 페이지 반환
		}
	
		// 비밀번호 찾기 처리 
		@PostMapping("/user/pw_search")
		public String pwSearch(@RequestParam("uid") String userId, Model model) {
		    // 사용자 ID를 사용하여 비밀번호 찾기 로직 구현
		    // 임시 비밀번호 발급 또는 비밀번호 재설정 링크를 이메일로 전송
		    return "user/pw_result"; // 결과 페이지 반환
		}
		
	

	//회원 수정 페이지
	//@AuthenticationPrincipal - 회원을 인가하는 클래스
	@GetMapping("/user/userupdate")
	public String updateUserForm(
			@AuthenticationPrincipal SecurityUser principal,
			Model model) {
		UsersDTO usersDTO = usersService.findByUid(principal);
		model.addAttribute("users", usersDTO);
		return "user/userupdate";
	}
		
	
	//회원 수정 처리 - 상세보기로 이동
	@PostMapping("/user/userupdate")
	public String update(@ModelAttribute UsersDTO usersDTO,
	            MultipartFile uimage, MultipartFile bgmFile, Model model) throws Exception {
	    usersService.saveFiles(usersDTO, uimage, bgmFile);
	    model.addAttribute("users", usersDTO);
	    return "redirect:/user/userpage/" + usersDTO.getUno();
	}

	// 장바구니 이동
	@GetMapping("/user/mybag")
	public String myBag() {
		return "user/mybag";
	}
	

		
}
