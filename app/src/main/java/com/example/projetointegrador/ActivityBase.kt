import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.projetointegrador.R
import com.google.android.material.navigation.NavigationView

open class BaseActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity_base)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
    }

    override fun setContentView(layoutResID: Int) {
        val container = findViewById<FrameLayout>(R.id.baseContainer)
        layoutInflater.inflate(layoutResID, container, true)

        // Depois que a tela for inflada, pegamos o botão menu
        val menuButton = findViewById<ImageView>(R.id.ic_menu)
        menuButton?.setOnClickListener {
            drawerLayout.openDrawer(navigationView)
        }
    }
}
