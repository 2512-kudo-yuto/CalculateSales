package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) throws IOException {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		
		//listFilesを使⽤してfilesという配列に、 
		//指定したパスに存在する全てのファイル(または、ディレクトリ)の情報を格納します。
		File[] files = new File(args[0]).listFiles();
		//先にファイルの情報を格納する List(ArrayList) を宣⾔します。
		List<File> rcdFiles = new ArrayList<>(); 
		
		
		//filesの数だけ繰り返すことで、 
		//指定したパスに存在する全てのファイル(または、ディレクトリ)の数だけ繰り返されます。 
		for(int i = 0; i < files.length ; i++) { 
			if (files[i].isFile() && files[i].getName().matches("^\\d{8}\\.rcd$")) {
				// 売上ファイルの条件に当てはまったものだけ、List(ArrayList) に追加します。
				rcdFiles.add(files[i]);
			} 
		} 
		
		//rcdFilesに複数の売上ファイルの情報を格納しているので、その数だけ繰り返します。
				for (int i = 0; i < rcdFiles.size(); i++) {

					BufferedReader br = new BufferedReader(new FileReader(rcdFiles.get(i)));

					// 売上ファイルの中身（1行目: 支店コード、2行目: 売上金額）を保持するList
					List<String> fileContents = new ArrayList<>();
					String line;

					while ((line = br.readLine()) != null) {
						fileContents.add(line);
					}
					br.close();

					// 1行目の支店コードと2行目の売上金額を取得
					String branchCode = fileContents.get(0);
					String saleStr = fileContents.get(1);

					//売上ファイルから読み込んだ売上金額をMapに加算していくために、型の変換を行います。
					long fileSale = Long.parseLong(saleStr);

					//読み込んだ売上⾦額を加算します。
					Long saleAmount = branchSales.get(branchCode) + fileSale;

					// 加算した売上金額をMapに更新
					branchSales.put(branchCode, saleAmount);
				}



		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				
				
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				String[] items = line.split(",");
				
				// Mapに格納する。
				branchNames.put(items[0], items[1]);
				branchSales.put(items[0], Long.valueOf(0L));
			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) throws IOException {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		
		// ファイルと書き込み用オブジェクトの作成
		File file = new File(path, fileName);
	    FileWriter fw = new FileWriter(file);
	    BufferedWriter bw = new BufferedWriter(fw);
		
	    // 書き込み処理
	    for (String key : branchNames.keySet()) {
	        String line = key + "," + branchNames.get(key) + "," + branchSales.get(key);
	        bw.write(line);
	        bw.newLine();
	    }
	    
	    bw.close();
		return true;
	}

}

