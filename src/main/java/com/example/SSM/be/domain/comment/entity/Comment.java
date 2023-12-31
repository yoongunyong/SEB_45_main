package com.example.SSM.be.domain.comment.entity;

import com.example.SSM.be.domain.board.audit.Auditable;
import com.example.SSM.be.domain.board.entity.Board;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;


@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Comment extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String author;




}
