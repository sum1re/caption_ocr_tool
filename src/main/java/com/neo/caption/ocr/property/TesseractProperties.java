package com.neo.caption.ocr.property;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@ConfigurationProperties(prefix = "tesseract")
public class TesseractProperties {

    /**
     * 0    Legacy engine only.<br/>
     * 1    Neural nets LSTM engine only. (Default)<br/>
     * 2    Legacy + LSTM engines.<br/>
     * 3    Based on what is available.
     */
    @Min(0)
    @Max(3)
    private int ocrEngineMode = 1;

    /**
     * 0    Orientation and script detection (OSD) only.<br/>
     * 1    Automatic page segmentation with OSD.<br/>
     * 2    Automatic page segmentation, but no OSD, or OCR. (not implemented)<br/>
     * 3    Fully automatic page segmentation, but no OSD.<br/>
     * 4    Assume a single column of text of variable sizes.<br/>
     * 5    Assume a single uniform block of vertically aligned text.<br/>
     * 6    Assume a single uniform block of text.<br/>
     * 7    Treat the image as a single text line. (Default)<br/>
     * 8    Treat the image as a single word.<br/>
     * 9    Treat the image as a single word in a circle.<br/>
     * 10   Treat the image as a single character.<br/>
     * 11   Sparse text. Find as much text as possible in no particular order.<br/>
     * 12   Sparse text with OSD.<br/>
     * 13   Raw line. Treat the image as a single text line.
     */
    @Min(0)
    @Max(13)
    private int pageSegMode = 7;

    private String supportedLanguage;

    private Config[] configs;

    @Getter
    @Setter
    public static class Config {

        @NotBlank
        private String name;
        @NotBlank
        private String value;

        @Override
        public String toString() {
            return "Tesseract parameter: " + name + "\t" + value;
        }

    }

}
