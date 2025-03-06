package commons;

import java.util.Locale;

public class AppConfig {
    private Collection selectedCollection;
    private Locale selectedLanguage;

    /**
     * Standard constructor used when new AppConfig is needed.
     */
    public AppConfig() {
    }

    /**
     * Overloaded constructor, used for when data/fields of the AppConfig is already known.
     *
     * @param collection the collection of notes that the user currently is working in
     * @param locale     the current language that the user has chosen
     */
    public AppConfig(Collection collection, Locale locale) {
        this.selectedCollection = collection;
        this.selectedLanguage = locale;
    }

    /**
     * Gets the latest selected Collection by the user.
     *
     * @return latest selected Collection
     */
    public Collection getSelectedCollection() {
        return selectedCollection;
    }

    /**
     * Sets the selected Collection that was last used.
     *
     * @param selectedCollection used to save the latest used Collection
     */
    public void setSelectedCollection(Collection selectedCollection) {
        this.selectedCollection = selectedCollection;
    }

    /**
     * Gets the latest selected language by the user.
     *
     * @return the language that was last used
     */
    public Locale getSelectedLanguage() {
        return selectedLanguage;
    }

    /**
     * Sets the selected language that was last used.
     *
     * @param selectedLanguage used to save the latest used language
     */
    public void setSelectedLanguage(Locale selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
    }
}
