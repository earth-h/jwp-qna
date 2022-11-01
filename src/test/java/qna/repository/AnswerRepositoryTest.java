package qna.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static qna.domain.AnswerTest.A1;
import static qna.domain.AnswerTest.A2;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import qna.domain.Answer;

@DataJpaTest
public class AnswerRepositoryTest {

    @Autowired
    AnswerRepository answerRepository;

    @Test
    void 답변을_저장하면_저장한_답변_객체를_반환한다() {
        //when
        Answer answer = answerRepository.save(A1);

        //then
        assertAll(
                () -> assertThat(answer.getId()).isNotNull(),
                () -> assertThat(answer.getContents()).isEqualTo(A1.getContents()),
                () -> assertThat(answer.isDeleted()).isFalse()
        );
    }

    @TestFactory
    Collection<DynamicTest> 저장된_답변의_삭제여부에_따라_findByIdAndDeletedFalse메소드_조회_결과가_다르게_반환된다() {
        //given
        Answer saveAnswer = answerRepository.save(A2);
        saveAnswer.setDeleted(false);
        Long saveAnswerId = saveAnswer.getId();
        return Arrays.asList(
                DynamicTest.dynamicTest("저장한 답변은 findByIdAndDeletedFalse()로 조회하면 정상적으로 조회가 된다.", () -> {
                    //when
                    Optional<Answer> findAnswer = answerRepository.findByIdAndDeletedFalse(saveAnswerId);

                    //then
                    assertThat(findAnswer).isPresent();
                    assertAll(
                            () -> assertThat(findAnswer.get().getContents()).isEqualTo(saveAnswer.getContents()),
                            () -> assertThat(findAnswer.get().isDeleted()).isFalse()
                    );
                }),
                DynamicTest.dynamicTest("저장한 답변의 삭제여부를 true로 업데이트하면, 더 이상 findByIdAndDeletedFalse() 조회 시 조회되지 않는다.", () -> {
                    //when
                    saveAnswer.setDeleted(true);
                    Optional<Answer> findAnswer = answerRepository.findByIdAndDeletedFalse(saveAnswerId);

                    //then
                    assertThat(findAnswer).isNotPresent();
                })
        );
    }

    @TestFactory
    Collection<DynamicTest> 답변을_저장하면_조회가_되지만_해당_답변을_삭제하고_조회하면_더_이상_조회되지_않는다() {
        //given
        Answer saveAnswer = answerRepository.save(A1);
        Long saveAnswerId = saveAnswer.getId();
        return Arrays.asList(
                DynamicTest.dynamicTest("저장한 답변의 id로 답변을 조회하면 정상적으로 조회가 된다.", () -> {
                    //when
                    Optional<Answer> findAnswer = answerRepository.findById(saveAnswerId);

                    //then
                    assertThat(findAnswer).isPresent();
                }),
                DynamicTest.dynamicTest("저장한 답변을 삭제하고, 다시 조회하면 해당 답변이 조회되지 않는다.", () -> {
                    //when
                    answerRepository.delete(saveAnswer);
                    Optional<Answer> deleteAnswer = answerRepository.findById(saveAnswerId);

                    //then
                    assertThat(deleteAnswer).isNotPresent();
                })
        );
    }
}
