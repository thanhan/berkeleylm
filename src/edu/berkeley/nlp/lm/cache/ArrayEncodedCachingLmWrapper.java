package edu.berkeley.nlp.lm.cache;

import edu.berkeley.nlp.lm.AbstractArrayEncodedNgramLanguageModel;
import edu.berkeley.nlp.lm.ArrayEncodedNgramLanguageModel;

/**
 * This class wraps {@link ArrayEncodedNgramLanguageModel} with a cache.
 * 
 * This wrapper is <b>not</b> threadsafe. To use a cache in a multithreaded
 * environment, you should create one wrapper per thread.
 * 
 * @author adampauls
 * 
 * @param <W>
 */
public class ArrayEncodedCachingLmWrapper<W> extends AbstractArrayEncodedNgramLanguageModel<W>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final ArrayEncodedLmCache cache;

	private final ArrayEncodedNgramLanguageModel<W> lm;

	private ArrayEncodedCachingLmWrapper(final ArrayEncodedNgramLanguageModel<W> lm) {
		this(lm, new ArrayEncodedDirectMappedLmCache(24, lm.getLmOrder()));
	}

	private ArrayEncodedCachingLmWrapper(final ArrayEncodedNgramLanguageModel<W> lm, final ArrayEncodedLmCache cache) {
		super(lm.getLmOrder(), lm.getWordIndexer(), Float.NaN);
		this.cache = cache;
		this.lm = lm;

	}

	@Override
	public float getLogProb(final int[] ngram, final int startPos, final int endPos) {
		final int hash = Math.abs(hash(ngram, startPos, endPos)) % cache.capacity();
		float f = cache.getCached(ngram, startPos, endPos, hash);
		if (!Float.isNaN(f)) return f;
		f = lm.getLogProb(ngram, startPos, endPos);
		cache.putCached(ngram, startPos, endPos, f, hash);

		return f;
	}

	private static int hash(final int[] key, final int startPos, final int endPos) {
		int hashCode = 1;
		for (int i = startPos; i < endPos; ++i) {

			final int curr = key[i];
			hashCode = 13 * hashCode + curr;
		}
		return hashCode;
	}

	/**
	 * To use a cache in a multithreaded environment, you should create one
	 * wrapper per threadÍ.
	 * 
	 * @param <T>
	 * @param lm
	 * @return
	 */
	public static <W> ArrayEncodedCachingLmWrapper<W> wrapWithCacheNotThreadSafe(ArrayEncodedNgramLanguageModel<W> lm) {
		return new ArrayEncodedCachingLmWrapper<W>(lm);
	}

	/**
	 * To use a cache in a multithreaded environment, you should create one
	 * wrapper per threadÍ.
	 * 
	 * @param <T>
	 * @param lm
	 * @return
	 */
	public static <W> ArrayEncodedCachingLmWrapper<W> wrapWithCacheNotThreadSafe(ArrayEncodedNgramLanguageModel<W> lm, ArrayEncodedLmCache cache) {
		return new ArrayEncodedCachingLmWrapper<W>(lm, cache);
	}

}
