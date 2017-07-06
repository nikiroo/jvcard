package be.nikiroo.jvcard.resources;


/**
 * This class manages the translation of {@link TransBundle.StringId}s into
 * user-understandable text.
 * 
 * @author niki
 * 
 */
public class TransBundle extends
		be.nikiroo.utils.resources.TransBundle<StringId> {

	/**
	 * Create a translation service with the default language.
	 */
	public TransBundle() {
		super(StringId.class, Target.resources);
	}

	/**
	 * Create a translation service for the given language. (Will fall back to
	 * the default one i not found.)
	 * 
	 * @param language
	 *            the language to use
	 */
	public TransBundle(String language) {
		super(StringId.class, Target.resources, language);
	}
}
