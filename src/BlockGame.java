import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;


public class BlockGame {

    static class MyFrame extends JFrame {

        // constant
        static int BALL_WIDTH = 20; // 공의 크기 지정
        static int BALL_HEIGHT = 20;
        static int BLOCK_ROWS = 5; // 블록의 줄 수
        static int BLOCK_COLUMNS = 10; // 가로 세로 10개
        static int BLOCK_WIDTH = 40; // 하나의 블록의 크기
        static int BLOCK_HEIGHT = 20;
        static int BLOCK_GAP = 3; // 블록과 블록사이의 간격
        static int BAR_WIDTH = 180; // 사용자가 움직일 바
        static int BAR_HEIGHT = 20;
        static int CANVAS_WIDTH = 400 + (BLOCK_GAP * BLOCK_COLUMNS) - BLOCK_GAP; // 전체 캔버스의 크기, 갭의 크기 더해주고 마지막 갭만 빼줌.
        static int CANVAS_HEIGHT = 600;

        // variable
        static MyPanel myPanel = null; // 그림판 역할 패널 생성
        static int score = 0; // 점수 변수 선언
        static Timer timer = null; // 타이머
        static Block[][] blocks = new Block[BLOCK_ROWS][BLOCK_COLUMNS]; // 세로 5개  가로 10개 공간만 만듬
        static Bar bar = new Bar();
        static Ball ball = new Ball();
        static int barXTarget = bar.x; // 조작을 보강해주는
        static int dir = 0; // 공이 움직이는 방향, 0 - Up-Right, 1 - Down-Right, 2 - Up-Left, 3 - Down-Left
        static int ballSpeed = 10; // 공의 속도
        static boolean isGameFinish = false;


        // 공
        static class Ball {
            int x = CANVAS_WIDTH / 2 - BALL_WIDTH / 2; // 캔버스의 중간에서 공의 너비 반지름만큼 빼준다. 중앙에 나오도록
            int y = CANVAS_HEIGHT / 2 - BALL_HEIGHT / 2;
            int width = BALL_WIDTH;
            int height = BALL_HEIGHT;

            // 충돌 체크용
            Point getCenter() {
                return new Point(x + (BALL_WIDTH / 2), y + (BALL_HEIGHT / 2));
            }

            Point getBottomCenter() { // 볼의 밑의지점
                return new Point(x + (BALL_WIDTH / 2), y + (BALL_HEIGHT));
            }

            Point getTopCenter() {
                return new Point(x + (BALL_WIDTH / 2), y);
            }

            Point getLeftCenter() {
                return new Point(x, y + (BALL_HEIGHT / 2));
            }

            Point getRightCenter() {
                return new Point(x + (BALL_WIDTH), y + (BALL_HEIGHT / 2));
            }
        }

        // 유저 바
        static class Bar {
            int x = CANVAS_WIDTH / 2 - BAR_WIDTH / 2;
            int y = CANVAS_HEIGHT - 100;
            int width = BAR_WIDTH;
            int height = BAR_HEIGHT;
        }

        // 블록
        static class Block {
            int x = 0;
            int y = 0;
            int width = BLOCK_WIDTH;
            int height = BLOCK_HEIGHT;
            int color = 0; // 0 - white(10점), 1 - yellow(20점), 2 - blue(30점), 3 - magenta(40점), 4 - red(50점), 색깔에 따라 얻는 점수 다르게 할 예정
            boolean isHidden = false; // 충돌 이후에, 블록이 사라지도록
        }

        // Panel UI 구성
        static class MyPanel extends JPanel {
            // 생성자
            public MyPanel() {
                this.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);
                this.setBackground(Color.BLACK);
            }

            @Override  // JPanel 에 지정되어 있는 함수 paint 오버라이딩
            public void paint(Graphics g) {
                super.paint(g); // 위쪽(super)에 paint 를 넘겨주면서 내가 생성됬다 초기화됬다는걸 알려준다.
                Graphics2D g2d = (Graphics2D) g;

                drawUI(g2d); // 실질적으로 그리는 코드
            }

            // 실질적으로 UI 그리는 코드
            private void drawUI(Graphics2D g2d) {
                //  draw Block
                for (int i = 0; i < BLOCK_ROWS; i++) {
                    for (int j = 0; j < BLOCK_COLUMNS; j++) {
                        if (blocks[i][j].isHidden) {
                            continue;
                        }
                        if (blocks[i][j].color == 0) { // 컬러 화이트이면
                            g2d.setColor(Color.WHITE);
                        } else if (blocks[i][j].color == 1) { // 컬러 옐로우이면
                            g2d.setColor(Color.YELLOW);
                        } else if (blocks[i][j].color == 2) { // 컬러 블루이면
                            g2d.setColor(Color.BLUE);
                        } else if (blocks[i][j].color == 3) { // 컬러 마젠타이면
                            g2d.setColor(Color.MAGENTA);
                        } else if (blocks[i][j].color == 4) { // 컬러 레드이면
                            g2d.setColor(Color.RED);
                        }
                        g2d.fillRect(blocks[i][j].x, blocks[i][j].y,
                                blocks[i][j].width, blocks[i][j].height);
                    }

                    // draw Score, 점수판
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("TimesRoman", Font.BOLD, 20));
                    g2d.drawString("SCORE : " + score , CANVAS_WIDTH / 2 - 50, 20);
                    if(isGameFinish) {
                        g2d.setColor(Color.RED);
                        g2d.drawString("Game Finished !", CANVAS_WIDTH / 2 - 70, 50);
                    }


                    // draw Ball, 공 그리기
                    g2d.setColor(Color.WHITE);
                    g2d.fillOval(ball.x, ball.y, BALL_WIDTH, BALL_HEIGHT); // 원형

                    // draw Bar, 바 그리기
                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(bar.x, bar.y, BAR_WIDTH, BAR_HEIGHT); // 사각형

                }
            }
        }


        // 생성자, 창 구성
        public MyFrame(String title) {
            super(title);
            this.setVisible(true);
            this.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);
            this.setLocation(400, 300);
            this.setLayout(new BorderLayout());
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            initData(); // 초기화 메소드

            myPanel = new MyPanel(); // 패널 만들기
            this.add("Center", myPanel); // 생성된 패널 집어넣기

            setKeyListener(); // 키보드 입력 메소드
            startTimer();
        }

        // 데이터 초기화
        public void initData() {
            for (int i = 0; i < BLOCK_ROWS; i++) {
                for (int j = 0; j < BLOCK_COLUMNS; j++) {
                    blocks[i][j] = new Block(); // 만들어둔 공간에, 실제로 객체 생성
                    blocks[i][j].x = BLOCK_WIDTH * j + BLOCK_GAP * j; // j가 가로
                    blocks[i][j].y = 100 + i * BLOCK_HEIGHT + BLOCK_GAP * i; // i가 세로
                    blocks[i][j].width = BLOCK_WIDTH;
                    blocks[i][j].height = BLOCK_HEIGHT;
                    blocks[i][j].color = 4 - i; // 0 - white(10점), 1 - yellow(20점), 2 - blue(30점), 3 - magenta(40점), 4 - red(50점)
                    blocks[i][j].isHidden = false;
                }
            }
        }

        // 키보드 입력받는 메소드
        public void setKeyListener() {
            this.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_LEFT) { // 화살표 왼쪽 키보드 눌리면
                        // 키보드 입력이 끊기지않고 부드럽게 하기 위한 코딩
                        barXTarget -= 20;
                        if (bar.x < barXTarget) { // 계속해서 키보드를 눌렀을 경우..
                            barXTarget = bar.x;
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) { // 화살표 오른쪽 키보드 눌리면
                        barXTarget += 20;
                        if (bar.x > barXTarget) { // 계속해서 키보드를 눌렀을 경우..
                            barXTarget = bar.x;
                        }
                    }
                }
            });
        }

        // 타이머 스타트
        public void startTimer() {
            timer = new Timer(20, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) { // Timer Evert
                    movement();
                    checkCollision(); // 벽, 바에 충돌처리
                    checkCollisionBlock(); // 50개의 블록에 충돌처리
                    myPanel.repaint(); // Redraw !

                    isGameFinish(); // Game Success

                }
            });
            timer.start(); // start Timer !
        }

        // 게임 클리어
        public  void isGameFinish() {
            int count = 0;
            for (int i = 0; i < BLOCK_ROWS; i++) {
                for (int j = 0; j < BLOCK_COLUMNS; j++) {
                    Block block = blocks[i][j];
                    if(block.isHidden)
                        count++;
                }
            }
            if(count == BLOCK_ROWS * BLOCK_COLUMNS) {
                    isGameFinish = true;
            }
        }

        // 바가 움직이는 코드
        public void movement() {
            if (bar.x < barXTarget) {
                bar.x += 5;
            } else if (bar.x > barXTarget) {
                bar.x -= 5;
            }

            // 볼의 움직임
            if (dir == 0) { // 0 : Up-Right
                ball.x += ballSpeed;
                ball.y -= ballSpeed;
            } else if (dir == 1) { // 1 : Down-Right
                ball.x += ballSpeed;
                ball.y += ballSpeed;
            } else if (dir == 2) { // 2 : Up-Left
                ball.x -= ballSpeed;
                ball.y -= ballSpeed;
            } else if (dir == 3) { // 3 : Down-Left
                ball.x -= ballSpeed;
                ball.y += ballSpeed;
            }

        }

        // 충돌 했는지 안했는지
        public boolean duplRect(Rectangle rect1, Rectangle rect2) {
            return rect1.intersects(rect2); // 체크 두개의 사각형 중복 되는지
        }

        //  벽, 바에 충돌처리 코드
        public void checkCollision() {
            if (dir == 0) { // 0 : Up-Right
                // Wall
                if (ball.y < 0) { // 위쪽 벽에 부딪힌 것
                    dir = 1; // 방향을 틀어준다.
                }
                if (ball.x > CANVAS_WIDTH - BALL_WIDTH) { // 오른쪽 벽에 부딪힌 것
                    dir = 2;
                }

                // Bar - none
            } else if (dir == 1) { // 1 : Down-Right
                // Wall
                if (ball.y > CANVAS_HEIGHT - BALL_HEIGHT - BALL_HEIGHT) { // 아래쪽 벽에 부딪힌 것
                    dir = 0; // 방향을 틀어준다.

                    // 아래로 공 내려가면, game reset
                    dir = 0;
                    ball.x = CANVAS_WIDTH / 2 - BALL_WIDTH / 2;
                    ball.y = CANVAS_HEIGHT / 2 - BALL_HEIGHT / 2;
                    score = 0;
                }
                if (ball.x > CANVAS_WIDTH - BALL_WIDTH) { // 오른쪽 벽에 부딪힌 것
                    dir = 3;
                }

                // Bar
                if (ball.getBottomCenter().y >= bar.y) {
                    if (duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height),
                            new Rectangle(bar.x, bar.y, bar.width, bar.height))) { // 충돌이 된 것
                        dir = 0;
                    }
                }
            } else if (dir == 2) { // 2 : Up-Left
                // Wall
                if (ball.y < 0) { // 위쪽 벽에 부딪힌 것
                    dir = 3;
                }
                if (ball.x < 0) { // 왼쪽 벽에 부딪힌 것
                    dir = 0;
                }
                // Bar - none

            } else if (dir == 3) { // 3 : Down-Left
                // Wall
                if (ball.y > CANVAS_HEIGHT - BALL_HEIGHT - BALL_HEIGHT) { // 아래쪽 벽에 부딪힌 것
                    dir = 2;

                    // 아래로 공 내려가면, game reset
                    dir = 0;
                    ball.x = CANVAS_WIDTH / 2 - BALL_WIDTH / 2;
                    ball.y = CANVAS_HEIGHT / 2 - BALL_HEIGHT / 2;
                    score = 0;
                }
                if (ball.x < 0) { // 왼쪽 벽에 부딪힌 것
                    dir = 1;
                }
                // Bar
                if (ball.getBottomCenter().y >= bar.y) {
                    if (duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height),
                            new Rectangle(bar.x, bar.y, bar.width, bar.height))) { // 충돌이 된 것
                        dir = 2;
                    }
                }
            }
        }

        // 50개의 블록에 충돌처리 코드
        public void checkCollisionBlock() {
            for (int i = 0; i < BLOCK_ROWS; i++) {
                for (int j = 0; j < BLOCK_COLUMNS; j++) {
                    Block block = blocks[i][j];
                    if (block.isHidden == false) {
                        if (dir == 0) { // 0 : Up-Right
                            // 블록하고 공이 충돌 했을 때
                            if (duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height),
                                    new Rectangle(block.x, block.y, block.width, block.height))) { // 충돌이 된 것
                                if (ball.x > block.x + 2 &&
                                        ball.getRightCenter().x <= block.x + block.width - 2) {
                                    // 블록의 아래쪽 충돌
                                    dir = 1;
                                } else {
                                    // 블록의 왼쪽에 충돌
                                    dir = 2;
                                }
                                block.isHidden = true;
                                blockScore(block);
                            }
                        } else if (dir == 1) { // 1 : Down-Right
                            if (duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height),
                                    new Rectangle(block.x, block.y, block.width, block.height))) { // 충돌이 된 것
                                if (ball.x > block.x + 2 &&
                                        ball.getRightCenter().x <= block.x + block.width - 2) {
                                    // 블록의 위쪽 충돌
                                    dir = 0;
                                } else {
                                    // 블록의 왼쪽에 충돌
                                    dir = 3;
                                }
                                block.isHidden = true;
                                blockScore(block);
                            }
                        } else if (dir == 2) { // 2 : Up-Left
                            if (duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height),
                                    new Rectangle(block.x, block.y, block.width, block.height))) { // 충돌이 된 것
                                if (ball.x > block.x + 2 &&
                                        ball.getRightCenter().x <= block.x + block.width - 2) {
                                    // 블록의 아래쪽 충돌
                                    dir = 3;
                                } else {
                                    // 블록의 오른쪽에 충돌
                                    dir = 0;
                                }
                                block.isHidden = true;
                                blockScore(block);
                            }
                        } else if (dir == 3) { // 3 : Down-Left
                            if (duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height),
                                    new Rectangle(block.x, block.y, block.width, block.height))) { // 충돌이 된 것
                                if (ball.x > block.x + 2 &&
                                        ball.getRightCenter().x <= block.x + block.width - 2) {
                                    // 블록의 위쪽 충돌
                                    dir = 2;
                                } else {
                                    // 블록의 오른쪽에 충돌
                                    dir = 1;
                                }
                                block.isHidden = true;
                                blockScore(block);
                            }
                        }
                    }
                }
            }

        }

        // 블록의 점수
        private void blockScore(Block block) {
            if(block.color == 0) {
                score += 10;
            } else if(block.color == 1) {
                score += 20;
            } else if(block.color == 2) {
                score += 30;
            } else if(block.color == 3) {
                score += 40;
            } else if(block.color == 4) {
                score += 50;
            }
        }
    }
        // 메인이 static 이라서 보통 static 으로 생성
        public static void main(String[] args) {

            new MyFrame("Block Game");
        }
    }