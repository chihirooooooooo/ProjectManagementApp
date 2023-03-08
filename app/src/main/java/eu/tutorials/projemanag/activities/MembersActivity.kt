package eu.tutorials.projemanag.activities

import android.app.Activity
import android.app.Dialog
import android.app.usage.ConfigurationStats
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import eu.tutorials.projemanag.R
import eu.tutorials.projemanag.adapters.MemberListItemsAdapter
import eu.tutorials.projemanag.firebase.FirestoreClass
import eu.tutorials.projemanag.models.Board
import eu.tutorials.projemanag.models.User
import eu.tutorials.projemanag.utils.Constants
import kotlinx.android.synthetic.main.activity_members.*
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.dialog_search_member.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MembersActivity : BaseActivity() {
    private lateinit var mBoardDetails: Board
    private lateinit var mAssignedMemberList: ArrayList<User>
    private var anyChangeMade: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members)
        if(intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getAssignedMembersListDetails(this, mBoardDetails.assignedTo)
        }
        setupActionBar()


    }

    fun setupMembersList(list: ArrayList<User>){
        mAssignedMemberList = list
        hideProgressDialog()
        rv_members_list.layoutManager = LinearLayoutManager(this)
        rv_members_list.setHasFixedSize(true)

        val adapter = MemberListItemsAdapter(this, list)
        rv_members_list.adapter = adapter
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_members_activity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.members)
        }
        toolbar_members_activity.setNavigationOnClickListener{
            onBackPressed()
        }
    }

    fun memberDetails(user: User) {
        mBoardDetails.assignedTo.add(user.id)
        FirestoreClass().assignMemberToBoard(this, mBoardDetails, user)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.memu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_add_member -> {
                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun dialogSearchMember(){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)
        dialog.tv_add.setOnClickListener{
            val email = dialog.et_email_search_member.text.toString()
            if(email.isNotEmpty()){
                dialog.dismiss()
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().getMemberDetails(this, email)
            }else{
                Toast.makeText(
                    this@MembersActivity,
                    "Please enter members email address",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        dialog.tv_cancel.setOnClickListener{
            dialog.dismiss()
        }

        dialog.show()

    }

    fun memberAssignSuccess(user: User){
        hideProgressDialog()
        mAssignedMemberList.add(user)
        anyChangeMade = true
        setupMembersList(mAssignedMemberList)
        SendNotificationToUserAsyncTask(mBoardDetails.name, user.fcmToken ).execute()
    }

    override fun onBackPressed() {
        if(anyChangeMade){
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    private inner class SendNotificationToUserAsyncTask(val boardName: String, val token: String): AsyncTask<Any, Void, String>(){
        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog(resources.getString(R.string.please_wait))

        }
        override fun doInBackground(vararg p0: Any?): String {
            var result : String
            var connection : HttpURLConnection? = null
            try{
                val url = URL(Constants.FCM_BASE_URL)
                connection = url.openConnection() as HttpURLConnection
                connection.doOutput = true
                connection.doInput = true
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty(Constants.FCM_AUTHORIZATION, "${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}")
                connection.useCaches = false
                val wr = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                val dataObject = JSONObject()
                dataObject.put(Constants.FCM_KEY_TITLE, "Assigned to the board $boardName")
                dataObject.put(Constants.FCM_KEY_MESSAGE, "You have been assigned to the Board by ${mAssignedMemberList[0].name}")
                jsonRequest.put(Constants.FCM_KEY_DATA, dataObject)
                jsonRequest.put(Constants.FCM_KEY_TO, token)
                wr.writeBytes(jsonRequest.toString())
                wr.flush()
                wr.close()
                val httpResult: Int = connection.responseCode
                if(httpResult == HttpURLConnection.HTTP_OK){
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val sb = StringBuilder()
                    var line: String?
                    try{
                        while(reader.readLine().also{line = it} != null){
                            sb.append(line + "\n")
                        }
                    }catch (e: IOException){
                        e.printStackTrace()
                    }finally {
                        try{
                            inputStream.close()
                        }catch(e: IOException){
                            e.printStackTrace()
                        }
                    }
                    result = sb.toString()
                }else{
                    result = connection.responseMessage
                }
            }catch (e: SocketTimeoutException){
                result = "Connection Timeout"
            }catch(e: Exception){
                result = "Error: " + e.message
            }finally{
                connection?.disconnect()
            }


            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            hideProgressDialog()
            Log.e("JSON Response Result", result!!)
        }

    }
}