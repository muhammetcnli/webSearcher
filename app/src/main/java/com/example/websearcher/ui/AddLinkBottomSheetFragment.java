package com.example.websearcher.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.websearcher.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

// AddLinkBottomSheetFragment, kullanıcıdan URL girmesini sağlayan bir alt sayfa (bottom sheet) içerir
// Kullanıcı URL'yi girdikten sonra, URL'yi dinleyici aracılığıyla dışarıya iletir
// URL girişinin tamamlanmasının ardından fragment kapatılır
// editTextUrl, URL'yi almak için kullanılır ve "Done" tuşuna basıldığında işlemi gerçekleştirir

public class AddLinkBottomSheetFragment extends BottomSheetDialogFragment {

    private OnUrlEnteredListener urlEnteredListener;

    public void setOnUrlEnteredListener(OnUrlEnteredListener listener) {
        this.urlEnteredListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_link_bottom_sheet, container, false);

        EditText editTextUrl = view.findViewById(R.id.editTextUrl);
        editTextUrl.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String url = editTextUrl.getText().toString().trim();

                if (!url.isEmpty() && urlEnteredListener != null) {
                    urlEnteredListener.onUrlEntered(url);
                }

                dismiss();
                return true;
            }
            return false;
        });

        return view;
    }

    public interface OnUrlEnteredListener {
        void onUrlEntered(String url);
    }
}
