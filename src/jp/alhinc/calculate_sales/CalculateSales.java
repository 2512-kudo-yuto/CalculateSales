package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// 商品定義ファイル名
	private static final String FILE_NAME_COMMODITY_LST = "commodity.lst";

	// 商品別集計ファイル名
	private static final String FILE_NAME_COMMODITY_OUT = "commodity.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "ファイルのフォーマットが不正です";
	private static final String SALESFILE_NOT_SEQUENTIAL = "売上ファイル名が連番になっていません";
	private static final String TOTAL_AMOUNT_OVERFLOW = "合計金額が10桁を超えました";
	private static final String CODE_INVALID_FORMAT = "コードが不正です";
	private static final String SALESFILE_INVALID_FORMAT = "のフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		// コマンドライン引数が渡されていない場合のエラー処理
		if (args.length != 1) {
			System.out.println(UNKNOWN_ERROR);
			return;
		}

		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new TreeMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new TreeMap<>();

		// 支店コードと商品名を保持するMap
		Map<String, String> commodityNames = new TreeMap<>();

		// 支店コードと商品名を保持するMap
		Map<String, Long> commoditySales = new TreeMap<>();

		// 支店定義ファイル読み込み処理
		if (!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales, "支店定義", "^[0-9]{3}$")) {
			return;
		}

		// 商品定義ファイル読み込み処理
		if (!readFile(args[0], FILE_NAME_COMMODITY_LST, commodityNames, commoditySales, "商品定義", "^[a-zA-Z0-9]{8}$")) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)

		//listFilesを使⽤してfilesという配列に、
		//指定したパスに存在する全てのファイル(または、ディレクトリ)の情報を格納します。

		File[] files = new File(args[0]).listFiles();
		if (files == null) {
			System.out.println(UNKNOWN_ERROR);
			return;
		}
		//先にファイルの情報を格納する List(ArrayList) を宣⾔します。
		List<File> rcdFiles = new ArrayList<>();

		//filesの数だけ繰り返すことで、
		//指定したパスに存在する全てのファイル(または、ディレクトリ)の数だけ繰り返されます。
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile() && files[i].getName().matches("^\\d{8}\\.rcd$")) {
				// 売上ファイルの条件に当てはまったものだけ、List(ArrayList) に追加します。
				rcdFiles.add(files[i]);
			}
		}

		// 追加：売上ファイルをファイル名の昇順にソート
		Collections.sort(rcdFiles);

		//  売上ファイルの連番チェック
		for (int i = 0; i < rcdFiles.size(); i++) {
			String fileName = rcdFiles.get(i).getName();
			int fileNo = Integer.parseInt(fileName.substring(0, 8));
			if (fileNo != i + 1) {
				System.out.println(SALESFILE_NOT_SEQUENTIAL);
				return;
			}
		}

		//rcdFilesに複数の売上ファイルの情報を格納しているので、その数だけ繰り返します。
		for (int i = 0; i < rcdFiles.size(); i++) {
			BufferedReader br = null;
			String fileName = rcdFiles.get(i).getName();

			try {
				br = new BufferedReader(new FileReader(rcdFiles.get(i)));

				// 売上ファイルの中身（1行目: 支店コード、2行目: 売上金額）を保持するList
				List<String> fileContents = new ArrayList<>();
				String line;

				while ((line = br.readLine()) != null) {
					fileContents.add(line);
				}

				// 売上ファイルの中身が3行ではない場合
				if (fileContents.size() != 3) {
					System.out.println(fileName + SALESFILE_INVALID_FORMAT);
					return;
				}

				// 各行のデータを取得
				String branchCode = fileContents.get(0);
				String commodityCode = fileContents.get(1);
				String saleStr = fileContents.get(2);

				// 支店コードが支店定義ファイルに該当しなかった場合
				if (!branchNames.containsKey(branchCode)) {
					System.out.println(fileName + "の支店" + CODE_INVALID_FORMAT);
					return;
				}

				// 商品コードが商品定義ファイルに該当しなかった場合
				if (!commodityNames.containsKey(commodityCode)) {
					System.out.println(fileName + "の商品" + CODE_INVALID_FORMAT);
					return;
				}

				// 売上金額が半角数字ではない場合
				if (!saleStr.matches("^[0-9]+$")) {
					System.out.println(UNKNOWN_ERROR);
					return;
				}

				//売上ファイルから読み込んだ売上金額をMapに加算していくために、型の変換を行います。
				//数字ではない場合のエラー処理
				long fileSale;
				try {
					fileSale = Long.parseLong(saleStr);
				} catch (NumberFormatException e) {
					System.out.println(UNKNOWN_ERROR);
					return;
				}

				//読み込んだ売上⾦額を加算します。
				Long saleAmount = branchSales.get(branchCode) + fileSale;
				Long commodityAmount = commoditySales.get(commodityCode) + fileSale;

				// 合計金額が10桁を超えた場合
				if (saleAmount >= 10000000000L || commodityAmount >= 10000000000L) {
					System.out.println(TOTAL_AMOUNT_OVERFLOW);
					return;
				}

				// 加算した売上金額をMapに更新
				branchSales.put(branchCode, saleAmount);
				commoditySales.put(commodityCode, commodityAmount);

			} catch (IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}
		}

		// 支店別集計ファイル書き込み処理
		if (!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

		// 商品別集計ファイル書き込み処理
		if (!writeFile(args[0], FILE_NAME_COMMODITY_OUT, commodityNames, commoditySales)) {
			return;
		}
	}

	/**
	 * 定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> names,
			Map<String, Long> sales, String fileTypeName, String codeRegex) {
		BufferedReader br = null;
		File file = new File(path, fileName);

		// 支店定義ファイルが存在しない場合
		if (!file.exists()) {
			System.out.println(fileTypeName + FILE_NOT_EXIST);
			return false;
		}

		try {
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while ((line = br.readLine()) != null) {

				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				String[] items = line.split(",");

				// 支店定義ファイルのフォーマットが不正な場合
				if (items.length != 2 || (!items[0].matches(codeRegex))) {
					System.out.println(fileTypeName + FILE_INVALID_FORMAT);
					return false;
				}

				// Mapに格納する。
				names.put(items[0], items[1]);
				sales.put(items[0], 0L);
			}

		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch (IOException e) {
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
	private static boolean writeFile(String path, String fileName, Map<String, String> names,
			Map<String, Long> sales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		BufferedWriter bw = null;

		// ファイルと書き込み用オブジェクトの作成
		try {
			File file = new File(path, fileName);
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);

			// 書き込み処理
			for (String key : names.keySet()) {
				String line = key + "," + names.get(key) + "," + sales.get(key);
				bw.write(line);
				bw.newLine();
			}
		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

}
