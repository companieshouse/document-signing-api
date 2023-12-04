package uk.gov.companieshouse.documentsigningapi.coversheet;

import org.apache.commons.lang.WordUtils;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.documentsigningapi.dto.CoverSheetDataDTO;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfRequestDTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;

@Component
public class FilingHistoryGenerator {

    public FilingHistoryGenerator() {
    }

    private static class Position {
        private final float x;
        private final float y;

        private Position(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * Extracts the first part of the filing history description from the CoverSheetDataDTO.
     * Searches for text enclosed in double asterisks within the full description and returns extracted content.
     * @param coverSheetDataDTO Coversheet Data containing filing history description
     * @return The extracted data, or simply the full filing history description provided if it is not in the expected double asterisks format
     */
    private String extractFilingHistoryDescriptionHead(final CoverSheetDataDTO coverSheetDataDTO) {
        String fullDescription = "";
        if (coverSheetDataDTO != null) {
            fullDescription = coverSheetDataDTO.getFilingHistoryDescription();

            if (fullDescription != null) {

                Pattern pattern = Pattern.compile("\\*\\*(.*?)\\*\\*");
                Matcher matcher = pattern.matcher(fullDescription);

                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        }
        return fullDescription;
    }

    /**
     * Extracts the second part of the filing history description from the CoverSheetDataDTO.
     * Searches for text enclosed in double asterisks within the full description, identifying the head part, and returns
     * the remaining content as the description tail.
     * @param coverSheetDataDTO Coversheet Data containing filing history description
     * @return The extracted data, or an empty string if there is no tail
     */
    private String extractFilingHistoryDescriptionTail(final CoverSheetDataDTO coverSheetDataDTO) {
        if (coverSheetDataDTO != null) {
            String fullDescription = coverSheetDataDTO.getFilingHistoryDescription();

            if (fullDescription != null) {

                Pattern pattern = Pattern.compile("\\*\\*(.*?)\\*\\*");
                Matcher matcher = pattern.matcher(fullDescription);

                if (matcher.find()) {
                    int endIndex = matcher.end();
                    return fullDescription.substring(endIndex);
                }
            }
        }
        return "";
    }

    /**
     * Builds the complete filing history tail by replacing placeholder values with the correct values from the SignPdfDataDTO
     * and appending the filing history type to the tail.
     * @param signPdfData Contains filing history description values for placeholder spots
     * @param coverSheetData contains additional filing history type value
     * @return the populated tail of the filing history description
     */
    private String buildFilingHistoryDescriptionTailWithValues(final SignPdfRequestDTO signPdfData, final  CoverSheetDataDTO coverSheetData) {
        String filingHistoryDescriptionTail = extractFilingHistoryDescriptionTail(coverSheetData);

        if(signPdfData != null && coverSheetData != null) {
            Map<String, String> descriptionValues = signPdfData.getFilingHistoryDescriptionValues();

            Map<String, String> formattedDescriptionValues = formatDateValuesInMap(descriptionValues);
            if (formattedDescriptionValues!= null) {
                filingHistoryDescriptionTail = replaceFilingHistoryDescriptionPlaceholders(filingHistoryDescriptionTail, formattedDescriptionValues);
            }
            filingHistoryDescriptionTail += " (" + coverSheetData.getFilingHistoryType() + ")";
        }
        return filingHistoryDescriptionTail;
    }

    /**
     * Replaces placeholders in the filing history description tail with values from the provided map.
     * Each placeholder in the tail is identified and replaced with its corresponding value.
     * @param input The original tail string containing placeholders to be replaced
     * @param placeholderValues A map of placeholders and their corresponding values
     * @return modified string with placeholder values correctly filled
     */
    private String replaceFilingHistoryDescriptionPlaceholders(String input, Map<String, String> placeholderValues) {
        if (placeholderValues != null) {
            for (Map.Entry<String, String> entry : placeholderValues.entrySet()) {
                String placeholder = "{" + entry.getKey() + "}";
                String replacement = entry.getValue();
                input = input.replace(placeholder, replacement);
            }
    }
        return input;
    }

    /**
     * Formats any values associated with a date to the correct date format
     * @param inputMap containing the filing history description values
     * @return An updated map with correct format of date values
     */
    public static Map<String, String> formatDateValuesInMap(Map<String, String> inputMap) {
        // Formatter for correct date output format
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy");

        // Create new map to store formatted values
        Map<String, String> formattedMap = new HashMap<>();

        if (inputMap != null) {
            // Iterate through entries in the filing history description values map
            for (Map.Entry<String, String> entry : inputMap.entrySet()) {
                // Check if any keys contains the word 'date'
                if (entry.getKey().toLowerCase().contains("date")) {
                    // Parse the date string and format it
                    LocalDate date = LocalDate.parse(entry.getValue(), DateTimeFormatter.ISO_LOCAL_DATE);
                    String formattedDate = date.format(dateTimeFormatter);
                    // Update new map with formatted key
                    formattedMap.put(entry.getKey(), formattedDate);
                } else {
                    // For keys that don't contain 'date', just add them to map as normal
                    formattedMap.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return formattedMap;
    }

    /**
     * Renders a full filing history description on the PDF, combining the extracted head and populated tail, wrapping the text if necessary.
     * @param coverSheetDataDTO Coversheet Data
     * @param signPdfRequestDTO Signpdf Request Data
     * @param font1 font1 for rendering HELVETICA_BOLD
     * @param font2 font2 for rendering HELVETICA
     * @param page The PDF page
     * @param contentStream The content stream for rendering
     * @param positionX The X-axis starting position on the page
     * @param positionY The Y-axis starting position on the page
     * @throws IOException If an I/O error occurs during rendering
     */
    public void renderFilingHistoryDescriptionWithBoldText(
                                    final CoverSheetDataDTO coverSheetDataDTO,
                                    final SignPdfRequestDTO signPdfRequestDTO,
                                    final Font font1,
                                    final Font font2,
                                    final PDPage page,
                                    final PDPageContentStream contentStream,
                                    final Float positionX,
                                    final Float positionY)
                                    throws IOException {

        final String filingHistoryDescriptionHead = extractFilingHistoryDescriptionHead(coverSheetDataDTO);
        final String filingHistoryDescriptionTail = buildFilingHistoryDescriptionTailWithValues(signPdfRequestDTO, coverSheetDataDTO);

        final Position position = new Position(positionX, positionY);


        // Combine head and tail into single text
        String combinedText = filingHistoryDescriptionHead + filingHistoryDescriptionTail;

        // Wrap combined text
        String[] wrappedText = WordUtils.wrap(combinedText, 60). split("\\r?\\n");

        contentStream.beginText();
        contentStream.newLineAtOffset(position.x, position.y);

        for (int i=0; i < wrappedText.length; i++) {
            String line = wrappedText[i];

            if(filingHistoryDescriptionHead != null){

                int headIndex = line.indexOf(filingHistoryDescriptionHead);

                if (headIndex >= 0) {
                    contentStream.setFont(font1.getPdFont(), font1.getSize());
                    contentStream.showText(line.substring(0, headIndex + filingHistoryDescriptionHead.length()));

                    contentStream.setFont(font2.getPdFont(), font2.getSize());
                    contentStream.showText(line.substring(headIndex + filingHistoryDescriptionHead.length()));

                    if (i < wrappedText.length - 1) {
                        contentStream.newLineAtOffset(0, -font1.getSize());
                    }
                } else {
                    contentStream.setFont(font2.getPdFont(), font2.getSize());
                    contentStream.showText(line);
                    if (i < wrappedText.length - 1) {
                        contentStream.newLineAtOffset(0, -font2.getSize());
                    }

                }
            }
        }
        contentStream.endText();
    };

}
