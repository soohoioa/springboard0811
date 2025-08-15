package com.project.board0811.service;

import java.util.List;

public interface BoardService {

    BoardResponseDto create(BoardRequestDto requestDto);
    BoardResponseDto getById(Long id);
    List<BoardResponseDto> getAll();
    BoardResponseDto update(Long id, BoardRequestDto requestDto);
    void delete(Long id);
}
