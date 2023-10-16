package tools;

public class StringUtils {
    public static String removeAllSpecialChars(String rawtxt){
        if(rawtxt == null)
            return(null);
        return(rawtxt.replaceAll("[^A-Za-z0-9]",""));
    }
    public static String removeCommonParts(String chaine1, String chaine2){
        String s1 = removeCommonParts(chaine1,chaine2,false);
        return(removeCommonParts(s1,chaine2,true));
    }
    public static String removeCommonParts(String chaine1, String chaine2, boolean backward){
        String difference = "";

        // Recherche de la partie différente entre les deux chaînes
        int i = backward ? chaine1.length() - 1 : 0;
        int j = backward ? chaine2.length() - 1 : 0;
        int pas = backward ? -1 : 1;

        while (i >= 0 && j >= 0 && i < chaine1.length() && j < chaine2.length() && chaine1.charAt(i) == chaine2.charAt(j)) {
            i += pas;
            j += pas;
        }

        if (backward) {
            if (i >= 0) {
                difference = chaine1.substring(0, i + 1);
            } else {
                difference = chaine1;
            }
        } else {
            if (i > 0) {
                difference = chaine1.substring(i);
            } else {
                difference = chaine1;
            }
        }

        return difference;
    }

    public static void main(String[] args){
        String s1 = "sparse_mp2rage_0p8_CS4p5_WIP925_sparse_mp2rage_0p8_CS4p5_WIP925_T1_Images_sparse_mp2rage_0p8_CS4p5_WIP925_20200122144656_7.nii";
        String s2 = "sparse_mp2rage_0p8_CS4p5_WIP925_sparse_mp2rage_0p8_CS4p5_WIP925_T1_Images_sparse_mp2rage_0p8_CS4p5_WIP925_20200122144656_7_ROI1.nii";
        System.out.println(removeCommonParts(s2,s1));
    }
}
