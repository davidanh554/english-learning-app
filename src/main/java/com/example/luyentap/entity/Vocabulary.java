package com.example.luyentap.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "vocabularies")
public class Vocabulary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    @Column(nullable = false)
    private String word;

    @Column(name = "loaitu")
    private String loaiTu;

    @Column(name = "phienam")
    private String phienAm;

    @Column(nullable = false)
    private String meaning;

    @Column(columnDefinition = "TEXT")
    private String example;

    // Các trường nâng cao mới bổ sung
    @Column(name = "word_forms", columnDefinition = "TEXT")
    private String wordForms;

    @Column(columnDefinition = "TEXT")
    private String paraphrase;

    @Column(name = "collocation_example", columnDefinition = "TEXT")
    private String collocationExample;

    @Column(name = "audio_us")
    private String audioUs;

    @Column(name = "audio_uk")
    private String audioUk;

    // Getters và Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getLoaiTu() { return loaiTu; }
    public void setLoaiTu(String loaiTu) { this.loaiTu = loaiTu; }

    public String getPhienAm() { return phienAm; }
    public void setPhienAm(String phienAm) { this.phienAm = phienAm; }

    public String getMeaning() { return meaning; }
    public void setMeaning(String meaning) { this.meaning = meaning; }

    public String getExample() { return example; }
    public void setExample(String example) { this.example = example; }

    public String getWordForms() { return wordForms; }
    public void setWordForms(String wordForms) { this.wordForms = wordForms; }

    public String getParaphrase() { return paraphrase; }
    public void setParaphrase(String paraphrase) { this.paraphrase = paraphrase; }

    public String getCollocationExample() { return collocationExample; }
    public void setCollocationExample(String collocationExample) { this.collocationExample = collocationExample; }

    public String getAudioUs() { return audioUs; }
    public void setAudioUs(String audioUs) { this.audioUs = audioUs; }

    public String getAudioUk() { return audioUk; }
    public void setAudioUk(String audioUk) { this.audioUk = audioUk; }
}