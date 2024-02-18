package com.work.community.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.work.community.dto.ItemDTO;
import com.work.community.dto.UserDiaryDTO;
import com.work.community.entity.UserDiary;
import com.work.community.repository.DiaryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class DiaryService {
	
	private final DiaryRepository diaryRepository;

	//글쓰기 처리
	public void save(UserDiaryDTO userDiaryDTO) {
	    UserDiary userDiary = UserDiary.saveToEntity(userDiaryDTO); // 수정된 부분
	    diaryRepository.save(userDiary);
	}
	
	//글목록 불러오기 (페이지 처리)
	public Page<UserDiaryDTO> findAll(Pageable pageable) {
		int page = pageable.getPageNumber() - 1; // db는 현재페이지보다 1 작아야함
		int pageSize = 9;
		pageable = PageRequest.of(page, pageSize, Sort.Direction.DESC, "dno");
		
		Page<UserDiary> diaryList = diaryRepository.findAll(pageable);
		
		log.info("diaryList.isFirst()=" + diaryList.isFirst());
		log.info("diaryList.isLast()=" + diaryList.isLast());
		log.info("diaryList.getNumber()=" + diaryList.getNumber());
		/*
		 * List<UserDiaryDTO> diaryDTOList = new ArrayList<>(); for(UserDiary userDiary
		 * : diaryList) { UserDiaryDTO memberDTO = UserDiaryDTO.toSaveDTO(userDiary);
		 * diaryDTOList.add(memberDTO); }
		 */
		Page<UserDiaryDTO> userDiaryDTOList = diaryList.map(diary ->
		new UserDiaryDTO(diary.getDno(), diary.getDtitle(), diary.getDcontent(), diary.getDfilename(), 
				diary.getDfilepath(), diary.getUsers(), diary.getCreatedDate(), diary.getUpdatedDate()));
		
		return userDiaryDTOList;
	}
	
	//글 상세보기
	public UserDiary findById(Integer dno) {
		return diaryRepository.findById(dno).get();
	}
	//글 삭제
	public void deleteById(Integer dno) {
		diaryRepository.deleteById(dno);
	}
	
	//글 수정 
	public void update(UserDiary userDiary) {
		diaryRepository.save(userDiary);
	}

	public UserDiary findByDno(Integer dno) {
        return diaryRepository.findById(dno).orElse(null);
    }

}
