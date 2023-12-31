package com.example.SSM.be.domain.board.service;

import com.example.SSM.be.domain.board.dto.BoardPatchDto;
import com.example.SSM.be.domain.board.dto.BoardPostDto;
import com.example.SSM.be.domain.board.dto.BoardResponseDto;
import com.example.SSM.be.domain.board.entity.Board;

import com.example.SSM.be.domain.board.entity.Image;
import com.example.SSM.be.domain.board.mapper.BoardMapper;
import com.example.SSM.be.domain.board.repository.BoardRepository;

import com.example.SSM.be.domain.board.repository.ImageRepository;
import com.example.SSM.be.domain.member.entity.Member;
import com.example.SSM.be.domain.member.service.MemberService;
import com.example.SSM.be.global.exception.BusinessLogicException;
import com.example.SSM.be.global.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.webjars.NotFoundException;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final ImageRepository imageRepository;


    //게시글 생성하기
    public Member createBoard(Member member, BoardPostDto postDto) throws IOException {

        if (postDto.getImage()==null || postDto.getImage().isEmpty()) {
            Board board = BoardMapper.postDtoToBoardEntity(member, postDto);
            boardRepository.save(board);
        }

        else {
            Board board = BoardMapper.postDtoWithImageToBoardEntity(member, postDto);
            long saveId = boardRepository.save(board).getBoardId();
            Board findBoard = boardRepository.findById(saveId).get();
            for (MultipartFile imageFile : postDto.getImage()) {

                String originFileName = imageFile.getOriginalFilename();
                String saveFileName = System.currentTimeMillis() + "_" + originFileName;
                String savePath = "C:/Users/yoongunyong/Desktop/images/" + saveFileName;
//                String savePath = "/Users/yungeon-yong/springboot_img/" + saveFileName;
                imageFile.transferTo(new File(savePath));


                Image image = BoardMapper.boardToImage(findBoard, originFileName, saveFileName);
                imageRepository.save(image);
            }
        }
        return member;
    }
    //특정 게시글 상세보기
    @Transactional
    public BoardResponseDto findById(long boardId,Boolean notYet) {
        if(!notYet){
            Optional<Board> optionalBoard = boardRepository.findById(boardId);
            if (optionalBoard.isPresent()) {
                Board board = optionalBoard.get();
                board.setView(board.getView() + 1);
                Board boardUpdated = boardRepository.save(board);
                Optional<Board> boardOptional = boardRepository.findById(board.getBoardId());
                if (boardOptional.isPresent()) {
                    Board findBoard = boardOptional.get();
                    BoardResponseDto responseDto = BoardMapper.boardToBoardResponseDto(findBoard);
                    return responseDto;
                } else {
                    return null;
                }
            }

        }else{
            Optional<Board> optionalBoard = boardRepository.findById(boardId);
            if (optionalBoard.isPresent()) {
                Board findBoard = optionalBoard.get();
                BoardResponseDto responseDto = BoardMapper.boardToBoardResponseDto(findBoard);
                return responseDto;
            }

        }
        return null;
    }
    //게시글 수정하기
    @Transactional
    public Board updateBoard(Member member, long boardId, BoardPatchDto patchDto) throws IOException {
        Board existingBoard = findByBoardId(boardId);

        if (member.getEmail().equals(existingBoard.getMember().getEmail())) {
            // 기존 이미지 삭제
            if (existingBoard.getImageList() != null) {
                for (Image image : existingBoard.getImageList()) {
                    // 이미지 파일 경로 생성
                    String existingImagePath = "C:/Users/yoongunyong/Desktop/images/" + image.getSaveFileName();

                    try {
                        // 이미지 파일 삭제
                        Files.deleteIfExists(Paths.get(existingImagePath));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // 이미지 엔티티 리스트 초기화
            existingBoard.getImageList().clear();

            if (patchDto.getImage()==null || patchDto.getImage().isEmpty()) {
                Board board = BoardMapper.boardPatchDtoToBoard(member, existingBoard ,patchDto);
                return boardRepository.save(board);
            }else{
                List<Image> newImages = new ArrayList<>();
                for (MultipartFile imageFile : patchDto.getImage()) {
                    String originFileName = imageFile.getOriginalFilename();
                    String saveFileName = System.currentTimeMillis() + "_" + originFileName;
                    String savePath = "C:/Users/yoongunyong/Desktop/images/" + saveFileName;

                    // 새 이미지 파일 저장
                    imageFile.transferTo(new File(savePath));

                    // 이미지 엔티티 생성 및 저장
                    Image image = new Image();
                    image.setOriginalFileName(originFileName);
                    image.setSaveFileName(saveFileName);
                    image.setBoard(existingBoard); // 부모 엔티티 설정

                    // 이미지를 부모 엔티티의 컬렉션에 추가
                    existingBoard.getImageList().add(image);

                    // 이미지 엔티티 저장
                    imageRepository.save(image);
                    newImages.add(image);
                }
                Board board = BoardMapper.patchDtoWithImageToBoardEntity(member, existingBoard, patchDto);
                Board board1 = boardRepository.save(board);
                return board1;
            }
            // 새 이미지 업로드 및 이미지 엔티티 생성 및 저장
            // 다른 업데이트 작업 수행 (이미지 외의 다른 필드 업데이트 등)

        } else {//기존 이
            throw new BusinessLogicException(ExceptionCode.NOT_MATCH_USER);
        }
    }
        /*
        @Transactional
public Board updateBoard(Member member, long boardId, BoardPatchDto patchDto) throws IOException {
    Board existingBoard = findByBoardId(boardId);

    if (member.getEmail().equals(existingBoard.getMember().getEmail())) {
        // 기존 이미지 삭제
        if (existingBoard.getImageList() != null) {
            for (Image image : existingBoard.getImageList()) {
                // 이미지 파일 경로 생성
                String existingImagePath = "C:/Users/yoongunyong/Desktop/images/" + image.getSaveFileName();

                try {
                    // 이미지 파일 삭제
                    Files.deleteIfExists(Paths.get(existingImagePath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 이미지 엔티티 리스트 초기화
        existingBoard.getImageList().clear();

        // 새 이미지 업로드 및 이미지 엔티티 생성 및 저장
        List<Image> newImages = new ArrayList<>();
        for (MultipartFile imageFile : patchDto.getImage()) {
            String originFileName = imageFile.getOriginalFilename();
            String saveFileName = System.currentTimeMillis() + "_" + originFileName;
            String savePath = "C:/Users/yoongunyong/Desktop/images/" + saveFileName;

            // 새 이미지 파일 저장
            imageFile.transferTo(new File(savePath));

            // 이미지 엔티티 생성 및 저장
            Image image = new Image();
            image.setOriginalFileName(originFileName);
            image.setSaveFileName(saveFileName);
            image.setBoard(existingBoard); // 부모 엔티티 설정

            // 이미지를 부모 엔티티의 컬렉션에 추가
            existingBoard.getImageList().add(image);

            // 이미지 엔티티 저장
            imageRepository.save(image);
            newImages.add(image);
        }

        // 다른 업데이트 작업 수행 (이미지 외의 다른 필드 업데이트 등)
        Board board = BoardMapper.patchDtoToBoardEntity(member, existingBoard, patchDto);
        boardRepository.save(board);

        return board;
    } else {
        throw new BusinessLogicException(ExceptionCode.NOT_MATCH_USER);
    }
}

         */
    //게시글 삭제하기
    public void deleteBoard(Member member, long boardId){
        Board findBoard = verifyBoardExist(boardId);
        if(member.getEmail()==findBoard.getMember().getEmail()){
            boardRepository.deleteById(boardId);
        }else{
            throw new BusinessLogicException(ExceptionCode.NOT_MATCH_USER);
        }
    }

    //게시글 존재 확인하기
    public Board verifyBoardExist(long boardId){
        Optional<Board> optionalBoard = boardRepository.findById(boardId);
        return optionalBoard.orElseThrow(() ->
                new BusinessLogicException(ExceptionCode.POST_NOT_FOUND));
    }
    public Board findByBoardId(long boardId){
        return boardRepository.findById(boardId).orElseThrow(()->new NotFoundException("ID에 해당하는 게시글이 없습니다:" + boardId));
    }


}