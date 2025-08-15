package com.project.board0811.domain.board.controller;

import com.project.board0811.domain.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {


    private final BoardService boardService;

    @GetMapping
    public String list(Model model) {
        List<BoardResponseDto> posts = boardService.getAll();
        System.out.println("게시글 개수 = " + posts.size()); // 확인용
        model.addAttribute("posts", posts);
        return "board/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        BoardResponseDto post = boardService.getById(id);
        model.addAttribute("post", post);
        return "board/detail";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("post", new BoardRequestDto());
        return "board/form";
    }

    @PostMapping
    public String create(BoardRequestDto requestDto) {
        BoardResponseDto saved = boardService.create(requestDto);
        return "redirect:/board/" + saved.getId();
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        BoardResponseDto post = boardService.getById(id);
        model.addAttribute("post", post);
        return "board/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam("_method") String method,
                         BoardRequestDto requestDto) {
        if ("put".equalsIgnoreCase(method)) {
            boardService.update(id, requestDto);
        }
        return "redirect:/board/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        boardService.delete(id);
        return "redirect:/board";
    }
}
