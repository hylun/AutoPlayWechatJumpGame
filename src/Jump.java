import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Random;

/**
 * Hylun(https://gitee.com/hylun)
 * 2018/1/2
 */
public class Jump {

    static String adb = "";
    static float rate = 1.33f;
    static boolean run = false;
    static boolean restart = true;
    private static int error = 0;

    public static void start() throws Exception {
        error = 0;
        while (run) {
            File pic = getScreenPic();
            jump(pic);
            Thread.sleep(2000+new Random().nextInt(1000));
        }
    }

    private static void jump(File pic) throws Exception {
        BufferedImage bi = ImageIO.read(pic);
        //获取图像的宽度和高度
        int width = bi.getWidth();
        int height = bi.getHeight();
        int x1=0,y1=0,x2=0,y2=0;
        //扫描图片,获取目标位置中心点
        for(int i=height/3;i<height*3/4;i++){
            int[] p1 = getRGB(bi,99, i);
            for(int j=100;j<width;j++){
                int[] p = getRGB(bi,j, i);
                if(p[0]>50 && p[0]<60 && p[1]>53 && p[1]<63){//黑棋比图高
                    j = j + 50;
                    continue;
                }
                if( Math.abs(p1[0]-p[0])>5 || Math.abs(p1[1]-p[1])>5 || Math.abs(p1[2]-p[2])>5 ){
                    x1 = j;
                    y1 = i;
                    break;
                }
            }
            if(y1 > 0){
                int[] p2 = getRGB(bi,x1, y1+10);
                while((i+=10)<height*3/4){
                    int[] p = getRGB(bi,x1, i);
                    if(Math.abs(p[0]-p2[0])>5 && Math.abs(p[1]-p2[1])>5 && Math.abs(p[2]-p2[2])>5 && (i-y1)>50){
                        y2 = i;
                        break;
                    }
                }
                y1 += (y2-y1)/2;
                break;
            }
        }
        //扫描获取黑棋位置
        for(int i=50;i<width;i++){
            int flag = 0;
            for(int j=height*3/4;j>height/3;j=j-5){
                int[] p = getRGB(bi,i, j);
                if(p[0]>50 && p[0]<60 && p[1]>53 && p[1]<63 && p[2]>95 && p[2]<105){
                    flag++;
                }
                if(flag>3){
                    x2 = i+5;
                    y2 = j;
                    break;
                }
            }
            if(x2 > 0) break;
        }
        double distance = Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
        if(x1<50 || y1<50 || x2<50 || y2<50 || distance<100){
            if(!restart || error>2) throw new Exception("scan error:"+x1+"|"+y1+"|"+x2+"|"+y2);
            int i = 1,j = 1;//获取重新开始按钮位置，自动重新开始
            for (;i<width && j>0;i=i+20){
                j = height-i;
                if(bi.getRGB(i, j)==-1) break;
            }
            error++;
            exec(adb+" shell input swipe "+(i+10)+" "+j+" "+(i+10)+" "+j+" 150");
            return;
        }
        if(distance < 200) distance = 200;
        int time = (int) (distance * rate);
        exec(adb+" shell input swipe "+x1+" "+y1+" "+x2+" "+y2+" "+time);
        File back = new File("backup.png");
        if(!back.exists() || back.delete())pic.renameTo(back);
    }

    private static int[] getRGB(BufferedImage bi,int x,int y){
        int rgb = bi.getRGB(x,y);
        int[] res = new int[3];
        res[0] = rgb >> 16 & 0xFF;
        res[1] = rgb >> 8 & 0xFF;
        res[2] = rgb & 0xFF;
        return res;
    }

    private static File getScreenPic() throws Exception {
        File pic = new File("pic.png");
        exec(adb+" shell screencap -p /sdcard/screen.png");
        exec(adb+" pull /sdcard/screen.png "+pic.getAbsolutePath());
        return pic;
    }

    static void exec(String cmd) throws Exception {
        System.out.println(cmd);
        Process ps = null;
        try {
            ps = Runtime.getRuntime().exec(cmd.split(" "));
            int code = ps.waitFor();
            if (code != 0) throw new Exception("exec error(code="+code+"): "+cmd);
        }finally{
            if(ps!=null) ps.destroy();
        }
    }
}
