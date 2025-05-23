package com.example.routes

import com.example.data.model.Note
import com.example.data.model.SimpleRequest
import com.example.data.model.User
import com.example.repository.Repo
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

const val NOTES = "$API_VERSION/notes"
const val CREATE_NOTES = "$NOTES/create"
const val UPDATE_NOTES = "$NOTES/update"
const val DELETE_NOTES = "$NOTES/delete"

@Location(CREATE_NOTES)
class NoteCreateRoute

@Location(NOTES)
class NoteGetRoute

@Location(UPDATE_NOTES)
class NoteUpdateRoute

@Location(DELETE_NOTES)
class NoteDeleteRoute

fun Route.NoteRoutes(
    db:Repo,
    hashFunction: (String)-> String
){
    authenticate("jwt") {
        post<NoteCreateRoute>{
            val note = try{
                call.receive<Note>()
            }catch (e:Exception){
                call.respond(HttpStatusCode.BadRequest, SimpleRequest(false,"Missing fields"))
                return@post
            }
            try {
                val email = call.principal<User>()!!.email
                db.addNote(note,email)
                call.respond(HttpStatusCode.OK, SimpleRequest(true,"Note added"))

            }catch (e:Exception){
                call.respond(HttpStatusCode.Conflict, SimpleRequest(false, e.message?:"Missing fields"))
            }

        }

        get<NoteGetRoute> {
            try{
                val email = call.principal<User>()!!.email
                val notes = db.getAllNotes(email)
                call.respond(HttpStatusCode.OK, notes)
            }catch (e:Exception){
                call.respond(HttpStatusCode.Conflict, SimpleRequest(false, e.message?:"Some problem"))
            }
        }
        post<NoteUpdateRoute>{
            val note = try{
                call.receive<Note>()
            }catch (e:Exception){
                call.respond(HttpStatusCode.BadRequest, SimpleRequest(false,"Missing fields"))
                return@post
            }
            try {
                val email = call.principal<User>()!!.email
                db.updateNote(note,email)
                call.respond(HttpStatusCode.OK, SimpleRequest(true,"Note updated"))

            }catch (e:Exception){
                call.respond(HttpStatusCode.Conflict, SimpleRequest(false, e.message?:"Missing fields"))
            }

        }
        delete<NoteDeleteRoute> {
            val noteId = try{
                call.request.queryParameters["id"]!!
            }catch (e:Exception){
                call.respond(HttpStatusCode.BadRequest, SimpleRequest(false,"wrong query parametr"))
                return@delete
            }

            try {
                val email = call.principal<User>()!!.email
                db.deleteNote(noteId,email)
                call.respond(HttpStatusCode.OK, SimpleRequest(true,"Note deleted"))
            }catch (e:Exception){
                call.respond(HttpStatusCode.Conflict, SimpleRequest(false, e.message?:"Missing fields"))

            }
        }
    }
}