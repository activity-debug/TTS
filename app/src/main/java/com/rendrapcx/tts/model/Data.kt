package com.rendrapcx.tts.model

class Data {

    data class Level(
        var id: String,
        var category: String,
        var dimension: String,
    )

    data class Question(
        var id: String,
        var number: Int,
        var direction: String,
        var asking: String,
        var answer: String,
        var slot: ArrayList<Int>,
        var levelId: String,
    )

    data class Partial(
        var id: String,
        var charAt: Int,
        var char: String,
        var rowQuestionId: String,
        var colQuestionId: String,
        var levelId: String,
    )

    companion object {
        var listLevel = mutableListOf<Level>(
            Level(id = "1", category = "Testing", dimension = "10x10")
        )
        var listQuestion = mutableListOf<Question>(
            Question(
                id = "bc511543-2",
                number = 0,
                direction = "HORIZONTAL",
                asking = "Ibukota Indonesia",
                answer = "JAKARTA",
                slot = arrayListOf<Int>(0, 1, 2, 3, 4, 5, 6),
                levelId = "1"
            ),
            Question(
                id = "a7eb3679-8",
                number = 0,
                direction = "VERTICAL",
                asking = "Nama kota di Papua",
                answer = "JAYAPURA",
                slot = arrayListOf<Int>(0, 10, 20, 30, 40, 50, 60, 70),
                levelId = "1"
            ),
            Question(
                id = "16234874-6",
                number = 90,
                direction = "HORIZONTAL",
                asking = "Ayahandanya Dawala",
                answer = "SEMAR",
                slot = arrayListOf<Int>(90, 91, 92, 93, 94),
                levelId = "1"
            ),
            Question(
                id = "d9b417d6-b",
                number = 2,
                direction = "VERTICAL",
                asking = "Batu besar di pinggir pantai",
                answer = "KARANG",
                slot = arrayListOf<Int>(2, 12, 22, 32, 42, 52),
                levelId = "1"
            ),
            Question(
                id = "e498373f-4",
                number = 50,
                direction = "HORIZONTAL",
                asking = "hewan berkaki dua bersayap",
                answer = "UNGGAS",
                slot = arrayListOf<Int>(50, 51, 52, 53, 54, 55),
                levelId = "1"
            ),
            Question(
                id = "74632f91-1",
                number = 5,
                direction = "VERTICAL",
                asking = "Tidak subur",
                answer = "TANDUS",
                slot = arrayListOf<Int>(5, 15, 25, 35, 45, 55),
                levelId = "1"
            ),
            Question(
                id = "a64f821f-b",
                number = 30,
                direction = "HORIZONTAL",
                asking = "Kata tanya",
                answer = "APA",
                slot = arrayListOf<Int>(30, 31, 32),
                levelId = "1"
            ),
            Question(
                id = "4b83e036-4",
                number = 22,
                direction = "HORIZONTAL",
                asking = "Hujan, ing",
                answer = "RAIN",
                slot = arrayListOf(22, 23, 24, 25),
                levelId = "1"
            ),
            Question(
                id = "a9d1d1fe-e",
                number = 70,
                direction = "HORIZONTAL",
                asking = "sesuatu hal yang diungkapkan sebagai argument",
                answer = "ALASAN",
                slot = arrayListOf<Int>(70, 71, 72, 73, 74, 75),
                levelId = "1"
            ),
            Question(
                id = "1f74700a-7",
                number = 71,
                direction = "VERTICAL",
                asking = "Berbohong, ing",
                answer = "LIE",
                slot = arrayListOf<Int>(71, 81, 91),
                levelId = "1"
            ),
            Question(
                id = "389570f7-e",
                number = 53,
                direction = "VERTICAL",
                asking = "Salah objek usaha BUMN",
                answer = "GAS",
                slot = arrayListOf<Int>(53, 63, 73),
                levelId = "1"
            ),
            Question(
                id = "a91d1ea6-8",
                number = 74,
                direction = "VERTICAL",
                asking = "Sumber kehidupan",
                answer = "AIR",
                slot = arrayListOf<Int>(74, 84, 94),
                levelId = "1"
            )


        )
        var listPartial = mutableListOf<Partial>(
            Partial(
                id = "fc1295a1-d",
                charAt = 0,
                char = "J",
                rowQuestionId = "bc511543-2",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "8eac9ce8-9",
                charAt = 1,
                char = "A",
                rowQuestionId = "bc511543-2",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "8a06126a-a",
                charAt = 2,
                char = "K",
                rowQuestionId = "bc511543-2",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "feac0125-d",
                charAt = 3,
                char = "A",
                rowQuestionId = "bc511543-2",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "67b0df0a-7",
                charAt = 4,
                char = "R",
                rowQuestionId = "bc511543-2",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "aeaa9349-6",
                charAt = 5,
                char = "T",
                rowQuestionId = "bc511543-2",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "d7829d0e-8",
                charAt = 6,
                char = "A",
                rowQuestionId = "bc511543-2",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "0fbc4f52-4",
                charAt = 0,
                char = "J",
                rowQuestionId = "bc511543-2",
                colQuestionId = "a7eb3679-8",
                levelId = "1"
            ),
            Partial(
                id = "444d3de1-2",
                charAt = 10,
                char = "A",
                rowQuestionId = "",
                colQuestionId = "a7eb3679-8",
                levelId = "1"
            ),
            Partial(
                id = "55e45b0c-c",
                charAt = 20,
                char = "Y",
                rowQuestionId = "",
                colQuestionId = "a7eb3679-8",
                levelId = "1"
            ),
            Partial(
                id = "f8a98281-f",
                charAt = 30,
                char = "A",
                rowQuestionId = "",
                colQuestionId = "a7eb3679-8",
                levelId = "1"
            ),
            Partial(
                id = "91b9bf7b-2",
                charAt = 40,
                char = "P",
                rowQuestionId = "",
                colQuestionId = "a7eb3679-8",
                levelId = "1"
            ),
            Partial(
                id = "7b3cbb4b-b",
                charAt = 50,
                char = "U",
                rowQuestionId = "",
                colQuestionId = "a7eb3679-8",
                levelId = "1"
            ),
            Partial(
                id = "62a25909-e",
                charAt = 60,
                char = "R",
                rowQuestionId = "",
                colQuestionId = "a7eb3679-8",
                levelId = "1"
            ),
            Partial(
                id = "149f42fd-0",
                charAt = 70,
                char = "A",
                rowQuestionId = "",
                colQuestionId = "a7eb3679-8",
                levelId = "1"
            ),
            Partial(
                id = "e14d020d-8",
                charAt = 90,
                char = "S",
                rowQuestionId = "16234874-6",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "92d1d3a4-1",
                charAt = 91,
                char = "E",
                rowQuestionId = "16234874-6",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "cfc9d008-0",
                charAt = 92,
                char = "M",
                rowQuestionId = "16234874-6",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "55624c39-8",
                charAt = 93,
                char = "A",
                rowQuestionId = "16234874-6",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "f49717da-4",
                charAt = 94,
                char = "R",
                rowQuestionId = "16234874-6",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "87f4b283-8",
                charAt = 2,
                char = "K",
                rowQuestionId = "",
                colQuestionId = "d9b417d6-b",
                levelId = "1"
            ),
            Partial(
                id = "5a3311bd-9",
                charAt = 12,
                char = "A",
                rowQuestionId = "",
                colQuestionId = "d9b417d6-b",
                levelId = "1"
            ),
            Partial(
                id = "6e2b2984-b",
                charAt = 22,
                char = "R",
                rowQuestionId = "",
                colQuestionId = "d9b417d6-b",
                levelId = "1"
            ),
            Partial(
                id = "a7629c35-e",
                charAt = 32,
                char = "A",
                rowQuestionId = "",
                colQuestionId = "d9b417d6-b",
                levelId = "1"
            ),
            Partial(
                id = "359b58ad-a",
                charAt = 42,
                char = "N",
                rowQuestionId = "",
                colQuestionId = "d9b417d6-b",
                levelId = "1"
            ),
            Partial(
                id = "fbdd2164-5",
                charAt = 52,
                char = "G",
                rowQuestionId = "",
                colQuestionId = "d9b417d6-b",
                levelId = "1"
            ),
            Partial(
                id = "1c01daf0-c",
                charAt = 50,
                char = "U",
                rowQuestionId = "e498373f-4",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "a9f2e1d0-d",
                charAt = 51,
                char = "A",
                rowQuestionId = "e498373f-4",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "29e0513b-d",
                charAt = 52,
                char = "N",
                rowQuestionId = "e498373f-4",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "ee67caeb-a",
                charAt = 53,
                char = "G",
                rowQuestionId = "e498373f-4",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "8eb5fe5e-1",
                charAt = 2,
                char = "K",
                rowQuestionId = "bc511543-2",
                colQuestionId = "d9b417d6-b",
                levelId = "1"
            ),
            Partial(
                id = "a1d00943-9",
                charAt = 12,
                char = "A",
                rowQuestionId = "",
                colQuestionId = "d9b417d6-b",
                levelId = "1"
            ),
            Partial(
                id = "c542191c-d",
                charAt = 22,
                char = "R",
                rowQuestionId = "",
                colQuestionId = "d9b417d6-b",
                levelId = "1"
            ),
            Partial(
                id = "5103018c-2",
                charAt = 32,
                char = "A",
                rowQuestionId = "",
                colQuestionId = "d9b417d6-b",
                levelId = "1"
            ),
            Partial(
                id = "1339915d-d",
                charAt = 42,
                char = "N",
                rowQuestionId = "",
                colQuestionId = "d9b417d6-b",
                levelId = "1"
            ),
            Partial(
                id = "bda1c686-f",
                charAt = 52,
                char = "G",
                rowQuestionId = "",
                colQuestionId = "d9b417d6-b",
                levelId = "1"
            ),
            Partial(
                id = "fabcdf42-6",
                charAt = 50,
                char = "U",
                rowQuestionId = "e498373f-4",
                colQuestionId = "a7eb3679-8",
                levelId = "1"
            ),
            Partial(
                id = "7027ed2f-e",
                charAt = 51,
                char = "N",
                rowQuestionId = "e498373f-4",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "e8ebee46-5",
                charAt = 52,
                char = "G",
                rowQuestionId = "e498373f-4",
                colQuestionId = "d9b417d6-b",
                levelId = "1"
            ),
            Partial(
                id = "24985679-7",
                charAt = 53,
                char = "G",
                rowQuestionId = "e498373f-4",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "425dd5fc-7",
                charAt = 54,
                char = "A",
                rowQuestionId = "e498373f-4",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "96994e4d-a",
                charAt = 55,
                char = "S",
                rowQuestionId = "e498373f-4",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "e2e9bf57-8",
                charAt = 5,
                char = "T",
                rowQuestionId = "bc511543-2",
                colQuestionId = "74632f91-1",
                levelId = "1"
            ),
            Partial(
                id = "69617000-9",
                charAt = 15,
                char = "A",
                rowQuestionId = "",
                colQuestionId = "74632f91-1",
                levelId = "1"
            ),
            Partial(
                id = "64bc8df1-2",
                charAt = 25,
                char = "N",
                rowQuestionId = "",
                colQuestionId = "74632f91-1",
                levelId = "1"
            ),
            Partial(
                id = "9fd4d005-6",
                charAt = 35,
                char = "D",
                rowQuestionId = "",
                colQuestionId = "74632f91-1",
                levelId = "1"
            ),
            Partial(
                id = "2abbc0b1-9",
                charAt = 45,
                char = "U",
                rowQuestionId = "",
                colQuestionId = "74632f91-1",
                levelId = "1"
            ),
            Partial(
                id = "d4ab72f7-f",
                charAt = 55,
                char = "S",
                rowQuestionId = "e498373f-4",
                colQuestionId = "74632f91-1",
                levelId = "1"
            ),
            Partial(
                id = "2ee07043-1",
                charAt = 30,
                char = "A",
                rowQuestionId = "a64f821f-b",
                colQuestionId = "a7eb3679-8",
                levelId = "1"
            ),
            Partial(
                id = "244b52d1-4",
                charAt = 31,
                char = "P",
                rowQuestionId = "a64f821f-b",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "82fc69ab-c",
                charAt = 32,
                char = "A",
                rowQuestionId = "a64f821f-b",
                colQuestionId = "d9b417d6-b",
                levelId = "1"
            ),
            Partial(
                id = "caafe4b6-6",
                charAt = 22,
                char = "R",
                rowQuestionId = "4b83e036-4",
                colQuestionId = "d9b417d6-b",
                levelId = "1"
            ),
            Partial(
                id = "7cd69e33-c",
                charAt = 23,
                char = "A",
                rowQuestionId = "4b83e036-4",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "01b60add-c",
                charAt = 24,
                char = "I",
                rowQuestionId = "4b83e036-4",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "50a647b2-6",
                charAt = 25,
                char = "N",
                rowQuestionId = "4b83e036-4",
                colQuestionId = "74632f91-1",
                levelId = "1"
            ),
            Partial(
                id = "75955974-8",
                charAt = 70,
                char = "A",
                rowQuestionId = "a9d1d1fe-e",
                colQuestionId = "a7eb3679-8",
                levelId = "1"
            ),
            Partial(
                id = "26faee6f-d",
                charAt = 71,
                char = "L",
                rowQuestionId = "a9d1d1fe-e",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "abff4ac5-4",
                charAt = 72,
                char = "A",
                rowQuestionId = "a9d1d1fe-e",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "a8c125ef-f",
                charAt = 73,
                char = "S",
                rowQuestionId = "a9d1d1fe-e",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "13af3f3c-7",
                charAt = 74,
                char = "A",
                rowQuestionId = "a9d1d1fe-e",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "9d49f62a-6",
                charAt = 75,
                char = "N",
                rowQuestionId = "a9d1d1fe-e",
                colQuestionId = "",
                levelId = "1"
            ),
            Partial(
                id = "849a362c-7",
                charAt = 71,
                char = "L",
                rowQuestionId = "a9d1d1fe-e",
                colQuestionId = "1f74700a-7",
                levelId = "1"
            ),
            Partial(
                id = "e75d31ab-9",
                charAt = 81,
                char = "I",
                rowQuestionId = "",
                colQuestionId = "1f74700a-7",
                levelId = "1"
            ),
            Partial(
                id = "b6cb5a66-a",
                charAt = 91,
                char = "E",
                rowQuestionId = "16234874-6",
                colQuestionId = "1f74700a-7",
                levelId = "1"
            ),
            Partial(
                id = "a0531ea5-d",
                charAt = 53,
                char = "G",
                rowQuestionId = "e498373f-4",
                colQuestionId = "389570f7-e",
                levelId = "1"
            ),
            Partial(
                id = "e224efe0-4",
                charAt = 63,
                char = "A",
                rowQuestionId = "",
                colQuestionId = "389570f7-e",
                levelId = "1"
            ),
            Partial(
                id = "f1c8c9ae-5",
                charAt = 73,
                char = "S",
                rowQuestionId = "a9d1d1fe-e",
                colQuestionId = "389570f7-e",
                levelId = "1"
            ),
            Partial(
                id = "1a75bc71-f",
                charAt = 74,
                char = "A",
                rowQuestionId = "a9d1d1fe-e",
                colQuestionId = "a91d1ea6-8",
                levelId = "1"
            ),
            Partial(
                id = "848e7dcf-7",
                charAt = 84,
                char = "I",
                rowQuestionId = "",
                colQuestionId = "a91d1ea6-8",
                levelId = "1"
            ),
            Partial(
                id = "634c3d11-a",
                charAt = 94,
                char = "R",
                rowQuestionId = "16234874-6",
                colQuestionId = "a91d1ea6-8",
                levelId = "1"
            )
        )


    }
}