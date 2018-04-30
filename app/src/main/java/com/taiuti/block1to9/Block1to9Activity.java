package com.taiuti.block1to9;

import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.taiuti.block1to9.core.Block1To9;
import com.taiuti.block1to9.model.Block;
import com.taiuti.block1to9.model.Match;

import java.util.Arrays;
import java.util.List;


// TODO add the music or special effects
public class Block1to9Activity extends AppCompatActivity {

    final String TAG = "Block1to9Activity";

    static final long AVAILABLEMOVES_TIMER = 3000;
    // side of the block ( it is a sqaure )
    final int SQUARE = 180;
    // sides of the game table
    int main_width, main_height;
    // sides of the block
    int block_width, block_height;
    // margin for the left side of the screen
    final float MARGIN = 20f;

    // game table
    private ViewGroup mainLayout;
    Drawable movedOnShape;
    Drawable touchedShape;
    Drawable normalShape;

    TextView tv_score;
    Match match;
    TextView tv_moves_available;
    int special_move = 0;

    Button btnAddRow;

    Block1To9 global;

    public Block1to9Activity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block1to9);

        global = (Block1To9) getApplication();

        block_width = (int) (SQUARE * global.getScaleX());
        block_height = (int) (SQUARE * global.getScaleY());


        mainLayout = (RelativeLayout) findViewById(R.id.block1to9_table);
        mainLayout.post(new Runnable() {
            @Override
            public void run() {
                main_width = (int) (mainLayout.getWidth() - MARGIN);
                main_height = mainLayout.getHeight();
                newGame();
            }
        });

        tv_moves_available = findViewById(R.id.tv_moves_available);
        tv_score = findViewById(R.id.tv_score);

        movedOnShape = getResources().getDrawable(R.drawable.block_moved_on);
        touchedShape = getResources().getDrawable(R.drawable.block_touched);
        normalShape = getResources().getDrawable(R.drawable.block);

        // bottone per inserire una nuova riga al gioco
        btnAddRow = findViewById(R.id.addRow);
        btnAddRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                match.resetMovesToNextRow();
                addRow();
            }
        });

        // bottone di riavvio del gioco
        final Button btnRestart = findViewById(R.id.restart);
        btnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(btnRestart.getContext());
                alertDialogBuilder.setTitle("Restart the game?");
                alertDialogBuilder
                        .setMessage("Do you want to start another game?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                                newGame();

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String syncConnPref = sharedPref.getString(SettingsActivity.AUDIO_SERVICE, "");

        match = new Match();

    }

    /**
     * set the game table
     */
    private void newGame() {

        mainLayout.removeAllViewsInLayout();
        tv_score.setText("0");

        match.startNewGame();

        for (int r = 0; r < match.LINETOSTART; r++) {
            addRow();
        }

        startAvailableMovesTimer();
    }

    /**
     * add row in the game table
     */
    private void addRow() {

        List<Block> blocks = match.addRow(this);

        for (int bl = 0; bl < blocks.size(); bl++) {

            Block block = blocks.get(bl);

            float x = MARGIN + block.getColumn() * main_width/match.COLUMNS;
            block.setX(x);
            block.setTextSize(32);
            block.setTextScaleX(global.getScaleX());
            block.setWidth(block_width);
            block.setHeight(block_height);

            block.setGravity(Gravity.CENTER);
            mainLayout.addView(block);

            if(match.isGameOver()) {
                animationBlockY(block, 0f, 0.5f*block_height*(match.ROWS - block.getRow()), 1000);
                stopAvailableMovesTimer();
            } else {
                animationBlockY(block, 0f, (match.ROWS - block.getRow() - 1) * main_height/match.ROWS, 1000);
                block.setOnTouchListener(blockOnTouchListener());
            }

        }

        if (match.isGameOver()) {

            // save the score in db and ask if you want start a new game
            match.saveTheScore();
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Game Over?");
            alertDialogBuilder
                    .setMessage("Game over." + "\n" + "A column is full." + "\n" + "Do you want to start another game?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            newGame();

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            finish();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }
    }

    private View.OnTouchListener blockOnTouchListener() {
        return new View.OnTouchListener() {
            float dX = 0, dY = 0;
            float startX = 0, startY = 0;
            float blockCenterPointX = 0, blockCenterPointY = 0;
            float blockCenterStartPointX = 0, blockCenterStartPointY = 0;

            float pointX = 0, pointY = 0;
            Block checked_block = null;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                int X = (int) motionEvent.getRawX();
                int Y = (int) motionEvent.getRawY();
                switch (motionEvent.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        // disable the timer
                        stopAvailableMovesTimer();
                        tv_moves_available.setVisibility(View.INVISIBLE);

                        startX = view.getX();
                        startY = view.getY();

                        Rect rect = new Rect();
                        view.getGlobalVisibleRect(rect);
                        View rootLayout = view.getRootView().findViewById(android.R.id.content);

                        int[] viewLocation = new int[2];
                        view.getLocationInWindow(viewLocation);

                        int[] rootLocation = new int[2];
                        rootLayout.getLocationInWindow(rootLocation);

                        blockCenterPointX = startX - view.getPivotX();
                        blockCenterPointY = startY - view.getPivotY();
                        blockCenterStartPointX = startX + view.getPivotX() + mainLayout.getX() + rootLocation[0];
                        blockCenterStartPointY = startY + view.getPivotY() + mainLayout.getY() + rootLocation[1];
                        pointX = motionEvent.getX();
                        pointY = motionEvent.getY();
                        dX = blockCenterPointX+pointX - motionEvent.getRawX();
                        dY = blockCenterPointY+pointY - motionEvent.getRawY();
                        view.animate()
                                .x(blockCenterPointX+pointX)
                                .y(blockCenterPointY+pointY)
                                .setDuration(0)
                                .start();

                        break;
                    case MotionEvent.ACTION_MOVE:
                        // move the block in the screen in a limitated ared
                        float moveX, moveY, stopX, stopY;
                        moveX = dX + motionEvent.getRawX();
                        moveY = dY + motionEvent.getRawY();
                        stopX = motionEvent.getRawX();
                        stopY = motionEvent.getRawY();

                        if (moveX > startX + view.getWidth() + 10f) {
                            moveX = startX + view.getWidth() + 10f;
                            stopX = blockCenterStartPointX + view.getWidth() + 10f;
                        }
                        if (moveX < startX - view.getWidth() - 10f) {
                            moveX = startX - view.getWidth() - 10f;
                            stopX = blockCenterStartPointX - view.getWidth() - 10f;
                        }
                        if (moveY > startY + view.getHeight() + 10f) {
                            moveY = startY + view.getHeight() + 10f;
                            stopY = blockCenterStartPointY + view.getHeight() + 10f;
                        }
                        if (moveY < startY - view.getHeight() - 10f) {
                            moveY = startY - view.getHeight() - 10f;
                            stopY = blockCenterStartPointY - view.getHeight() - 10f;
                        }

                        view.animate()
                                .x(moveX)
                                .y(moveY)
                                .setDuration(0)
                                .start();

                        // check if two block paired each other
                        checked_block = checkInBlock(stopX, stopY, (Block) view);
                        break;

                    case MotionEvent.ACTION_UP:
                        // if block is paired to another one it disappear and the new block will increase its value
                        if (checked_block != null) {
                            ViewGroup owner = (ViewGroup) view.getParent();

                            Block block_removed = (Block) view;
                            owner.removeView(view);

                            // update the matrix in match
                            checked_block = match.makeTheMove(checked_block, block_removed);

                            // update your score
                            tv_score.setText(String.valueOf(match.getScore()));

                            // reseat the table
                            reseatMatrix(owner, block_removed);

                            match.delAMoveToNextRow();

                            checked_block.setBackground(normalShape);
                            // if block arrive to 10, you can pair block in oblique way for SPECIAL_MOVES times
                            if(checked_block.getText().equals("10")) {
                                // special moves for prize to reach 10 point in a block
                                if (special_move < 0) {
                                    special_move = 0;
                                }
                                special_move += match.SPECIAL_MOVES;
                                owner.removeView(checked_block);
                                match.setRows(checked_block.getColumn(), match.getRows(checked_block.getColumn()) - 1);
                                reseatMatrix(owner, checked_block);
                            }

                            if(match.isMovesToNextRow()) {
                                addRow();
                            }

                            Log.i(TAG, Arrays.deepToString(match.getGameMatrix()));
                        } else {
                            // replace the block in the original place with animation
                            Block touched_block = (Block) view;

                            animationBlockX(touched_block, touched_block.getX(), MARGIN + touched_block.getColumn() * main_width/match.COLUMNS, 500);
                            animationBlockY(touched_block, touched_block.getY(), (match.ROWS - touched_block.getRow() - 1) * main_height/match.ROWS, 500);
                            view.setBackground(normalShape);
                        }
                        // enalbe the timer for available moves
                        startAvailableMovesTimer();
                    default:
                        return false;
                }
                return true;
            }
        };
    }

    private void animationBlockY(final Block block, float startPoint, float stopPoint, int duration) {

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(startPoint, stopPoint);
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float progress = (float) valueAnimator.getAnimatedValue();
                block.setTranslationY(progress);
                // no need to use invalidate() as it is already
                // present in the text view
            }
        });
        valueAnimator.start();
    }

    private void animationBlockX(final Block block, float startPoint, float stopPoint, int duration) {

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(startPoint, stopPoint);
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float progress = (float) valueAnimator.getAnimatedValue();
                block.setTranslationX(progress);
                // no need to use invalidate() as it is already
                // present in the text view
            }
        });
        valueAnimator.start();
    }

    private void reseatMatrix(ViewGroup owner, Block block_removed) {

        for (int i = 0; i < owner.getChildCount(); i++) {
            final Block block = (Block) owner.getChildAt(i);
            if (block.getColumn() == block_removed.getColumn()
                    && block.getRow() > block_removed.getRow()) {
                int row = block.getRow();
                block.setRow(row-1);

                animationBlockY(block, block.getY(), (match.ROWS - block.getRow() - 1) * main_height/match.ROWS, 500);
            }

        }
    }

    private Block checkInBlock(float x, float y, Block touched_block) {

        Block block_new = null;
        touched_block.setBackground(touchedShape);

        for (int child = 0; child < mainLayout.getChildCount(); child++) {
            Block block = (Block) mainLayout.getChildAt(child);
            if (block.get_Id() == touched_block.get_Id()) {
                continue;
            }

            Rect outRect = new Rect();
            block.getGlobalVisibleRect(outRect);

            if (outRect.contains((int) x, (int) y)) {

                if (touched_block.getText().equals(block.getText())) {
                    if (match.currentSpecialMoves > 0) {
                        block.setBackground(movedOnShape);
                        block_new = block;
                    } else if (touched_block.getRow()==block.getRow() ||
                                    touched_block.getColumn()==block.getColumn()) {
                        block.setBackground(movedOnShape);
                        block_new = block;
                    }
                }
            } else {
                block.setBackground(normalShape);
            }
        }
        return block_new;

    }

    private Handler availableMovesHandler = new Handler() {
        public void handlerMessage(Message msg) {
        }
    };

    /**
     * check available moves and blin the next move
     */
    private Runnable availableMovesCallback = new Runnable() {
        @Override
        public void run() {

            int moves = match.checkAvailableMoves();
            tv_moves_available.setText(getResources().getString(R.string.moves_available) + moves);
            tv_moves_available.setVisibility(View.VISIBLE);

            // blink the next move
            Animation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(50);
            anim.setStartOffset(20);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(10);

            if (moves > 0) {
                Integer[] nextBlocks = match.checkTheFirstMove();

                Block firstBlock = null;
                Block secondBlock = null;
                for (int child = 0; child < mainLayout.getChildCount(); child++) {

                    Block block1 = (Block) mainLayout.getChildAt(child);
                    if (nextBlocks[0] == block1.getRow() && nextBlocks[1] == block1.getColumn()) {
                        firstBlock = block1;
                    } else if (nextBlocks[2] == block1.getRow() && nextBlocks[3] == block1.getColumn()) {
                        secondBlock = block1;
                    }
                }
                firstBlock.startAnimation(anim);
                secondBlock.startAnimation(anim);
            } else {
                btnAddRow.startAnimation(anim);
            }

        }
    };

    private void startAvailableMovesTimer() {
        availableMovesHandler.removeCallbacks(availableMovesCallback);
        availableMovesHandler.postDelayed(availableMovesCallback, AVAILABLEMOVES_TIMER);
    }

    private void stopAvailableMovesTimer() {
        tv_moves_available.setVisibility(View.INVISIBLE);
        availableMovesHandler.removeCallbacks(availableMovesCallback);
    }

    public void onUserInteraction() {
        startAvailableMovesTimer();
    }

    public void onStop() {
        super.onStop();

        if (!match.isGameOver()) {
            // salvare matrice in memoria
            match.saveTheGame();
        }
        stopAvailableMovesTimer();
    }

}
