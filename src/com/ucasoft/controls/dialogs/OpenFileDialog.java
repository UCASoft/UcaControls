package com.ucasoft.controls.dialogs;

import android.annotation.TargetApi;
import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import com.ucasoft.controls.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

/**
 * Created by UCASoft.
 * User: Antonov Sergey
 * Date: 09.03.14
 * Time: 21:30
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class OpenFileDialog extends DialogFragment {

    private final String TAG = "OpenFileDialog";
    private String currentPath = Environment.getExternalStorageDirectory().getPath();
    private List<File> files = new ArrayList<File>();
    private TextView title;
    private ListView listView;
    private FilenameFilter filenameFilter;
    private int selectedIndex = -1;
    private OpenDialogListener listener;
    private Drawable folderIcon;
    private Drawable fileIcon;
    private String accessDeniedMessage;

    public interface OpenDialogListener {
        public void OnSelectedFile(String fileName);
    }

    private class FileAdapter extends ArrayAdapter<File> {

        public FileAdapter(Context context, List<File> files) {
            super(context, android.R.layout.simple_list_item_1, files);
        }

        private int getColor(int color) {
            return getContext().getResources().getColor(color);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getView(position, convertView, parent);
            File file = getItem(position);
            if (view != null) {
                view.setText(file.getName());
                if (file.isDirectory()) {
                    setDrawable(view, folderIcon);
                    view.setBackgroundColor(getColor(android.R.color.transparent));
                } else {
                    setDrawable(view, fileIcon);
                    if (selectedIndex == position)
                        view.setBackgroundColor(getColor(android.R.color.holo_blue_dark));
                    else
                        view.setBackgroundColor(getColor(android.R.color.transparent));
                }
            }
            return view;
        }

        private void setDrawable(TextView view, Drawable drawable) {
            if (view != null) {
                if (drawable != null) {
                    drawable.setBounds(0, 0, 60, 60);
                    view.setCompoundDrawables(drawable, null, null, null);
                } else {
                    view.setCompoundDrawables(null, null, null, null);
                }
            }
        }
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        title = createTitle(getActivity());
        changeTitle();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.open_file_dialog, null);
        prepareMainLayout(linearLayout);
        prepareBackItem((TextView) linearLayout.findViewById(R.id.open_file_dialog_back_button));
        listView = (ListView) linearLayout.findViewById(R.id.open_file_dialog_file_list);
        prepareListView(listView);
        listView.setCacheColorHint(getResources().getColor(android.R.color.transparent));
        files.addAll(getFiles(currentPath));
        listView.setAdapter(new FileAdapter(getActivity(), files));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setCustomTitle(title)
                .setView(linearLayout)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedIndex > -1 && listener != null) {
                            listener.OnSelectedFile(listView.getItemAtPosition(selectedIndex).toString());
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (listener == null) {
            try {
                listener = (OpenDialogListener) activity;
            } catch (ClassCastException e) {
                e.printStackTrace();
                listener = null;
            }
        }
    }

    public OpenFileDialog setFilter(final String filter) {
        filenameFilter = new FilenameFilter() {

            @Override
            public boolean accept(File file, String fileName) {
                File tempFile = new File(String.format("%s/%s", file.getPath(), fileName));
                return !tempFile.isFile() || tempFile.getName().matches(filter);
            }
        };
        return this;
    }

    public OpenFileDialog setOpenDialogListener(OpenDialogListener listener) {
        this.listener = listener;
        return this;
    }

    public OpenFileDialog setFolderIcon(Drawable drawable) {
        this.folderIcon = drawable;
        return this;
    }

    public OpenFileDialog setFileIcon(Drawable drawable) {
        this.fileIcon = drawable;
        return this;
    }

    public OpenFileDialog setAccessDeniedMessage(String message) {
        this.accessDeniedMessage = message;
        return this;
    }

    private static Display getDefaultDisplay(Context context) {
        return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    }

    private static Point getScreenSize(Context context) {
        Point screenSize = new Point();
        Display display = getDefaultDisplay(context);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
            screenSize.set(display.getWidth(), display.getHeight());
        } else
            display.getSize(screenSize);
        return screenSize;
    }

    private static int getLinearLayoutMinHeight(Context context) {
        return getScreenSize(context).y;
    }

    private void prepareMainLayout(LinearLayout linearLayout) {
        linearLayout.setMinimumHeight(getLinearLayoutMinHeight(getActivity()));
    }

    private int getItemHeight(Context context) {
        TypedValue value = new TypedValue();
        DisplayMetrics metrics = new DisplayMetrics();
        context.getTheme().resolveAttribute(android.R.attr.listPreferredItemHeightSmall, value, true);
        getDefaultDisplay(context).getMetrics(metrics);
        return (int) TypedValue.complexToDimension(value.data, metrics);
    }

    private TextView createTextView(Context context, int style) {
        TextView textView = new TextView(context);
        textView.setTextAppearance(context, style);
        int itemHeight = getItemHeight(context);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight));
        textView.setMinHeight(itemHeight);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setPadding(15, 0, 0, 0);
        return textView;
    }

    private TextView createTitle(Context context) {
        TextView textView = createTextView(context, android.R.style.TextAppearance_DeviceDefault_DialogWindowTitle);
        return textView;
    }

    private void prepareBackItem(TextView textView) {
        textView.setMinHeight(getItemHeight(getActivity()));
        textView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                File file = new File(currentPath);
                File parentDirectory = file.getParentFile();
                if (parentDirectory != null) {
                    currentPath = parentDirectory.getPath();
                    RebuildFiles(((FileAdapter) listView.getAdapter()));
                }
            }
        });
    }

    public int getTextWidth(String text, Paint paint) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.left + bounds.width() + 80;
    }

    private void changeTitle() {
        String titleText = currentPath;
        int screenWidth = getScreenSize(getActivity()).x;
        int maxWidth = (int) (screenWidth * 0.99);
        if (getTextWidth(titleText, title.getPaint()) > maxWidth) {
            while (getTextWidth("..." + titleText, title.getPaint()) > maxWidth) {
                int start = titleText.indexOf("/", 2);
                if (start > 0)
                    titleText = titleText.substring(start);
                else
                    titleText = titleText.substring(2);
            }
            title.setText("..." + titleText);
        } else {
            title.setText(titleText);
        }
    }

    private List<File> getFiles(String directoryPath) {
        File directory = new File(directoryPath);
        List<File> fileList = Arrays.asList(directory.listFiles(filenameFilter));
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File file, File file2) {
                if (file.isDirectory() && file2.isFile())
                    return -1;
                else if (file.isFile() && file2.isDirectory())
                    return 1;
                else
                    return file.getPath().compareTo(file2.getPath());
            }
        });
        return fileList;
    }

    private void RebuildFiles(ArrayAdapter<File> adapter) {
        try {
            List<File> fileList = getFiles(currentPath);
            files.clear();
            selectedIndex = -1;
            files.addAll(fileList);
            adapter.notifyDataSetChanged();
            changeTitle();
        } catch (NullPointerException e) {
            String message = getResources().getString(android.R.string.unknownName);
            if (accessDeniedMessage != null && !accessDeniedMessage.equals(""))
                message = accessDeniedMessage;
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void prepareListView(ListView listView) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                final ArrayAdapter<File> adapter = (FileAdapter) adapterView.getAdapter();
                File file = adapter.getItem(index);
                if (file.isDirectory()) {
                    currentPath = file.getPath();
                    RebuildFiles(adapter);
                } else {
                    if (index != selectedIndex)
                        selectedIndex = index;
                    else
                        selectedIndex = -1;
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }
}
