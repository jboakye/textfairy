package com.renard.documentview;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Pair;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.renard.ocr.DocumentContentProvider;
import com.renard.ocr.DocumentContentProvider.Columns;
import com.renard.ocr.R;
import com.renard.util.PreferencesUtils;
import com.viewpagerindicator.TitleProvider;

public class DocumentAdapter extends PagerAdapter implements TitleProvider {
	private Set<Integer> mChangedDocuments = new HashSet<Integer>();
	private SparseArray<Spanned> mSpannedTexts = new SparseArray<Spanned>();
	private SparseArray<CharSequence> mChangedTexts = new SparseArray<CharSequence>();

	private int mIndexTitle;
	private int mIndexOCRText;
	private int mIndexId;
	private LayoutInflater mInflater;
	private Context mContext;

	final Cursor mCursor;

	public DocumentAdapter(FragmentActivity activity, final Cursor cursor) {
		mCursor = cursor;
		mContext = activity.getApplicationContext();
		mIndexOCRText = mCursor.getColumnIndex(Columns.OCR_TEXT);
		// mIndexCreated = mCursor.getColumnIndex(Columns.CREATED);
		mIndexTitle = mCursor.getColumnIndex(Columns.TITLE);
		mIndexId = mCursor.getColumnIndex(Columns.ID);
		mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	@Override
	public Object instantiateItem(final View collection, final int position) {
		View view = null;
		if (mCursor.moveToPosition(position)) {
			final int documentId = mCursor.getInt(mIndexId);
			Spanned spanned  = mSpannedTexts.get(documentId);
			
			if (spanned==null) {
				final String text = mCursor.getString(mIndexOCRText);
				if (text==null){
					spanned=SpannableStringBuilder.valueOf("");
				} else {
					spanned = Html.fromHtml(text);
				}
				mSpannedTexts.put(documentId, spanned);
			}
			view = mInflater.inflate(R.layout.document_fragment, null);
			EditText edit = (EditText) view.findViewById(R.id.editText_document);
			edit.setText(spanned);
			TextWatcher watcher = new TextWatcher() {

				public void afterTextChanged(Editable s) {
				}

				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				public void onTextChanged(CharSequence s, int start, int before, int count) {
					mChangedDocuments.add(documentId);
					mChangedTexts.put(documentId, s);
				}
			};
			edit.addTextChangedListener(watcher);

			((ViewPager) collection).addView(view);
			PreferencesUtils.applyTextPreferences(edit, mContext);
		}
		return view;
	}

	public Pair<List<Uri>,List<Spanned>> getTextsToSave() {
		List<Uri> documentIds = new ArrayList<Uri>();
		List<Spanned> texts = new ArrayList<Spanned>();

		for (Integer id : mChangedDocuments) {
			documentIds.add(Uri.withAppendedPath(DocumentContentProvider.CONTENT_URI, String.valueOf(id)));
			final CharSequence text = mChangedTexts.get(id);
			texts.add((Spanned) text);
		}
		return new Pair<List<Uri>, List<Spanned>>(documentIds,texts);		
	}

	public void destroyItem(View collection, int position, Object view) {

		((ViewPager) collection).removeView((View) view);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == ((View) object);
	}

	// @Override
	// public Fragment getItem(int position) {
	// if (mCursor.moveToPosition(position)) {
	// final String text = mCursor.getString(mIndexOCRText);
	// Fragment fragment = DocumentFragment.newInstance(text);
	// return fragment;
	// }
	// return null;
	// }

	@Override
	public int getCount() {
		return mCursor.getCount();
	}

	public String getLongTitle(int position) {
		if (mCursor.moveToPosition(position)) {
			return mCursor.getString(mIndexTitle);
		}
		return null;
	}

	@Override
	public String getTitle(int position) {
		return String.valueOf(position + 1);
	}

}
