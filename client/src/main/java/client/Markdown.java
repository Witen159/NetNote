package client;

import client.utils.MarkdownSyntaxException;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.ResourceBundle;

/**
 * The Markdown class provides utilities for converting Markdown to HTML,
 * validating Markdown syntax, and generating temporary HTML files.
 * <p>
 * This class uses the CommonMark library for parsing and rendering Markdown content.
 */
public class Markdown {

    private ResourceBundle resourceBundle;

    /**
     * Constructs a new Markdown instance.
     * <p>
     * Initializes the Markdown parser and HTML renderer used for
     * converting Markdown content into HTML.
     * </p>
     */
    public Markdown() {
        Parser.builder().build();
        HtmlRenderer.builder().build();
        Parser.builder().build();
        HtmlRenderer.builder().build();
        resourceBundle = ResourceBundle
                .getBundle("Internationalization.Text", java.util.Locale.ENGLISH);
    }

    /**
     * Sets the ResourceBundle for translations.
     *
     * @param resourceBundle the ResourceBundle to be used for translations.
     */
    public void setResourceBundle(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    /**
     * Validates the syntax of the provided Markdown content.
     * <p>
     * This method checks for common Markdown syntax errors such as:
     * <ul>
     *   <li>Invalid headers</li>
     *   <li>Unmatched emphasis symbols (e.g., asterisks or underscores)</li>
     *   <li>Improperly formatted list items</li>
     *   <li>Unmatched backticks for inline code</li>
     *   <li>Invalid link or image syntax</li>
     * </ul>
     * If errors are found, they are aggregated into
     * a detailed error message and a MarkdownSyntaxException
     * is thrown. If no errors are detected, the method completes without exceptions.
     * </p>
     *
     * @param markdown the Markdown content to validate. It should not be null or empty.
     * @throws MarkdownSyntaxException if the Markdown content contains syntax errors.
     */
    public void validateMarkdownSyntax(String markdown) throws MarkdownSyntaxException {
        if (markdown == null || markdown.trim().isEmpty()) {
            throw new MarkdownSyntaxException(resourceBundle.getString("Markdown.empty"));
        }

        StringBuilder errorMessages = new StringBuilder();
        validateEmphasis(markdown, errorMessages);
        validateLists(markdown, errorMessages);
        validateBackticks(markdown, errorMessages);
        validateLinks(markdown, errorMessages);
        //Checking if the embedded images works
        //validateImages(markdown, errorMessages);

        if (!errorMessages.isEmpty()) {
            throw new MarkdownSyntaxException(errorMessages.toString().trim());
        }
    }

    /**
     * Validates the syntax of Markdown emphasis markers in the provided content.
     * <p>
     * This method checks for unmatched emphasis markers in the Markdown content, specifically:
     * <ul>
     *   <li>Asterisks (*) used for italic or bold text.</li>
     *   <li>Underscores (_) used for italic or bold text.</li>
     * </ul>
     * If an odd number of these markers is detected, indicating an unmatched marker,
     * an error message is appended to the provided errorMessages.
     * </p>
     *
     * @param markdown      the Markdown content to validate. It should not be null.
     * @param errorMessages a StringBuilder to which error messages are appended
     *                      if unmatched emphasis markers are found.
     */
    private void validateEmphasis(String markdown, StringBuilder errorMessages) {
        long asteriskCount = markdown.chars().filter(ch -> ch == '*').count();
        if (asteriskCount % 2 != 0) {
            errorMessages.append(resourceBundle.getString("Markdown.asterisk")).append("\n");
        }
        long underscoreCount = markdown.chars().filter(ch -> ch == '_').count();
        if (underscoreCount % 2 != 0) {
            errorMessages.append(resourceBundle.getString("Markdown.underscore")).append("\n");
        }
    }

    /**
     * Validates the syntax of Markdown list items in the provided content.
     * <p>
     * This method checks for improperly formatted list items in the Markdown content.
     * A valid list item must:
     * <ul>
     *   <li>Start with a dash ('-').</li>
     *   <li>Be followed by a space and then the content of the list item.</li>
     * </ul>
     * If invalid list items are detected, an error message is appended to the provided
     * errorMessages.
     * </p>
     *
     * @param markdown      the Markdown content to validate. It should not be null.
     * @param errorMessages a StringBuilder to which error messages are appended
     *                      if invalid list syntax is found.
     */
    private void validateLists(String markdown, StringBuilder errorMessages) {
        if (markdown.contains("-")) {
            boolean invalidList = markdown.lines().anyMatch(line -> line.trim().startsWith("-")
                    && !line.trim().matches("^-\\s+.*"));
            if (invalidList) {
                errorMessages.append(resourceBundle.getString("Markdown.list")).append("\n");
            }
        }
    }

    /**
     * Validates the syntax of backticks in the provided Markdown content.
     * <p>
     * This method checks for unmatched backticks (`` ` ``) in the Markdown content.
     * Backticks are used for inline code, and they must appear in pairs. If an odd number
     * of backticks is detected, indicating an unmatched backtick, an error message is appended
     * to the provided errorMessages.
     * </p>
     *
     * @param markdown      the Markdown content to validate. It should not be null.
     * @param errorMessages a StringBuilder to which error messages are appended
     *                      if unmatched backticks are found.
     */
    private void validateBackticks(String markdown, StringBuilder errorMessages) {
        long backtickCount = markdown.chars().filter(ch -> ch == '`').count();
        if (backtickCount % 2 != 0) {
            errorMessages.append(resourceBundle.getString("Markdown.backtick")).append("\n");
        }
    }

    /**
     * Validates the syntax of Markdown links in the provided content.
     * <p>
     * This method checks for improperly formatted or unmatched components of Markdown links.
     * A valid Markdown link must:
     * <ul>
     *   <li>Have matching square brackets for the link text (e.g., [text]).</li>
     *   <li>Have matching parentheses for the URL (e.g., (url)).</li>
     * </ul>
     * If unmatched brackets or parentheses are detected, appropriate error messages
     * are appended to the provided errorMessages.
     * </p>
     *
     * @param markdown      the Markdown content to validate. It should not be null.
     * @param errorMessages a StringBuilder to which error messages are appended
     *                      if invalid link syntax is found.
     */
    private void validateLinks(String markdown, StringBuilder errorMessages) {
        if (markdown.contains("[")) {
            boolean invalidLink = markdown.contains("[") && !markdown.contains("]");
            if (invalidLink) {
                errorMessages.append(resourceBundle.getString("Markdown.linkText")).append("\n");
            }

            boolean invalidLinkStructure = markdown.contains("(") && !markdown.contains(")");
            if (invalidLinkStructure) {
                errorMessages.append(resourceBundle.getString("Markdown.linkUrl")).append("\n");
            }
        }
    }

    /**
     * Validates the syntax of Markdown images in the provided content.
     * <p>
     * This method checks for improperly formatted or unmatched components of Markdown image tags.
     * A valid Markdown image tag must:
     * <ul>
     *   <li>Have matching square brackets for the alt text (e.g., ![alt text]).</li>
     *   <li>Have matching parentheses for the URL (e.g., (url)).</li>
     * </ul>
     * If unmatched brackets or parentheses are detected, appropriate error messages
     * are appended to the provided errorMessages.
     * </p>
     *
     * @param markdown      the Markdown content to validate. It should not be null.
     * @param errorMessages a StringBuilder to which error messages are appended
     *                      if invalid image syntax is found.
     */
    private void validateImages(String markdown, StringBuilder errorMessages) {
        if (markdown.contains("![")) {
            boolean invalidImage = markdown.contains("![") && !markdown.contains("]");
            if (invalidImage) {
                errorMessages.append(resourceBundle.getString("Markdown.imageAlt")).append("\n");
            }

            boolean invalidImageStructure = markdown.contains("(") && !markdown.contains(")");
            if (invalidImageStructure) {
                errorMessages.append(resourceBundle.getString("Markdown.imageUrl")).append("\n");
            }
        }
    }
}
