package qna.domain;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class AnswerTest {

	public static final Answer A1 = new Answer(UserTest.JAVAJIGI, QuestionTest.Q1, "Answers Contents1");
	public static final Answer A2 = new Answer(UserTest.SANJIGI, QuestionTest.Q1, "Answers Contents2");

	@Autowired
	private AnswerRepository answerRepository;

	@BeforeAll
	static void setUp(@Autowired UserRepository userRepository, @Autowired QuestionRepository questionRepository) {
		userRepository.save(UserTest.JAVAJIGI);
		userRepository.save(UserTest.SANJIGI);
		questionRepository.save(QuestionTest.Q1);
		questionRepository.save(QuestionTest.Q2);
	}

	@Test
	void 답변_저장_테스트() {
		// given // when
		Answer answer = answerRepository.save(A1);

		// then
		assertAll(
			() -> assertThat(answer.getCreatedDate()).isEqualTo(A1.getCreatedDate()),
			() -> assertThat(answer.getId()).isEqualTo(A1.getId()),
			() -> assertThat(answer.getQuestion()).isEqualTo(A1.getQuestion())
		);
	}

	@Test
	void 답변_조회_테스트() {
		// given
		Answer expectAnswer = answerRepository.save(A2);

		// when
		Optional<Answer> answer = answerRepository.findById(expectAnswer.getId());

		// then
		assertAll(
			() -> assertThat(answer.isPresent()).isTrue(),
			() -> assertThat(answer.get()).isEqualTo(expectAnswer)
		);
	}

	@Test
	void 답변_수정_테스트() {
		// given
		Answer answer = answerRepository.save(new Answer(UserTest.SANJIGI, QuestionTest.Q1, "Test1"));

		// when
		answer.setQuestion(QuestionTest.Q2);
		Answer expectAnswer = answerRepository.save(answer);

		// then
		assertThat(expectAnswer.getQuestion()).isEqualTo(QuestionTest.Q2);
	}

	@Test
	void 답변_삭제_테스트() {
		// given
		Answer answer = answerRepository.save(new Answer(UserTest.SANJIGI, QuestionTest.Q1, "Test1"));

		// when
		answerRepository.delete(answer);
		Optional<Answer> expectAnswer = answerRepository.findById(UserTest.SANJIGI.getId());

		// then
		assertThat(expectAnswer.isPresent()).isFalse();
	}
}
