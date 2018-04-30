package com.taiuti.block1to9.model;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.taiuti.block1to9.R;

/**
 * Block model.
 * It is a textView with a changing background
 */

public class Block extends AppCompatTextView {

    private int _id;
    private int column;
    private int row;
    private int value;

    public Block(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
        createBlock();
    }

    public Block(Context context) {
        super(context);
        createBlock();
    }

    public Block(Context context, AttributeSet attrs) {
        super(context, attrs);
        createBlock();
    }

    public void createBlock() {
        setBackground(getResources().getDrawable(R.drawable.block));
//        super.setWidth(190);
//        super.setHeight(190);
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int get_Id() {
        return _id;
    }

    public void set_Id(int id) {
        this._id = id;
    }

    public int getValue() { return value; }

    public void setValue(int value) {
        this.value = value;
        setText(String.valueOf(value));
    }

}
