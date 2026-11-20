package com.example.luyentap.service;

import com.example.luyentap.dto.QuizDTO;
import com.example.luyentap.entity.Quiz;
import com.example.luyentap.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;

    public List<QuizDTO> getByTopicId(Long topicId) {
        return quizRepository.findByTopicId(topicId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public QuizDTO getById(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy câu hỏi trắc nghiệm id = " + id));
        return toDTO(quiz);
    }

    public QuizDTO create(QuizDTO dto) {
        Quiz quiz = new Quiz();
        quiz.setQuestion(dto.getQuestion());
        quiz.setOptionA(dto.getOptionA());
        quiz.setOptionB(dto.getOptionB());
        quiz.setOptionC(dto.getOptionC());
        quiz.setOptionD(dto.getOptionD());
        quiz.setCorrectOption(dto.getCorrectOption());
        quiz.setTopicId(dto.getTopicId());

        return toDTO(quizRepository.save(quiz));
    }

    private QuizDTO toDTO(Quiz quiz) {
        return new QuizDTO(
                quiz.getId(),
                quiz.getQuestion(),
                quiz.getOptionA(),
                quiz.getOptionB(),
                quiz.getOptionC(),
                quiz.getOptionD(),
                quiz.getCorrectOption(),
                quiz.getTopicId()
        );
    }
}