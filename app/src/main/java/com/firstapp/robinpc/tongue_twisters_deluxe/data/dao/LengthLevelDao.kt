package com.firstapp.robinpc.tongue_twisters_deluxe.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.firstapp.robinpc.tongue_twisters_deluxe.data.model.LengthLevel
@Dao
interface LengthLevelDao {

    @Query("SELECT * FROM lengthlevel")
    fun getAllLengthLevels(): LiveData<List<LengthLevel>>

    @Query("SELECT count(title) FROM lengthlevel")
    fun getLengthLevelCount(): LiveData<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertLengthLevels(vararg params: LengthLevel)

    @Query("SELECT * FROM lengthlevel WHERE title = :title")
    fun getLengthLevel(title: String): LiveData<LengthLevel>
}