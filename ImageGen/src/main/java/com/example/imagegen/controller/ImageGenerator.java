package com.example.imagegen.controller;

import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ImageGenerator {

    @Autowired
    private OpenAiImageModel openaiImageModel;

    @GetMapping("/image")
    public Image getimage(@RequestParam String imagePrompt) {


        ImageResponse response = openaiImageModel.call(
                new ImagePrompt(imagePrompt,
                        OpenAiImageOptions.builder()
                                .quality("hd")
                                .N(1)
                                .height(1024)
                                .width(1024).build())
        );
        return response.getResult().getOutput();
    }





}
