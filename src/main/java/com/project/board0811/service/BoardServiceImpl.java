package com.project.board0811.service;

import com.project.board0811.domain.Board;
import com.project.board0811.dto.BoardRequestDto;
import com.project.board0811.dto.BoardResponseDto;
import com.project.board0811.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;

    @Transactional
    @Override
    public BoardResponseDto create(BoardRequestDto requestDto) {
        Board saved = boardRepository.save(requestDto.toEntity());
        return new BoardResponseDto(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public BoardResponseDto getById(Long id) {
        Board board = boardRepository.findById(id).orElse(null);
        return new BoardResponseDto(board);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BoardResponseDto> getAll() {
        return boardRepository.findAll()
                .stream()
                .map(BoardResponseDto::new)
                .toList();
    }

    @Transactional
    @Override
    public BoardResponseDto update(Long id, BoardRequestDto requestDto) {
        Board board = boardRepository.findById(id).orElse(null);
        board.updateBoard(requestDto.toEntity());
        return new BoardResponseDto(board);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Board board = boardRepository.findById(id).orElse(null);
        boardRepository.delete(board);
    }
}
